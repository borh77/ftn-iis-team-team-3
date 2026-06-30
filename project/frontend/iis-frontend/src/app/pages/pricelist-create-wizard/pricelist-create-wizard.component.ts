import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, NgZone, OnDestroy, OnInit, inject } from '@angular/core';
import {
  AbstractControl,
  ReactiveFormsModule,
  UntypedFormArray,
  UntypedFormBuilder,
  UntypedFormControl,
  UntypedFormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, finalize, forkJoin, map, Observable, of, Subscription, timeout } from 'rxjs';
import { extractBackendErrorMessage } from '../../core/http-error-message';
import { PortfolioService } from '../../core/portfolio.service';
import { Category, Product, Subcategory, Variant } from '../../core/portfolio.models';
import {
  Pricelist,
  PricelistCreationStep,
  PricelistItem,
  PricelistWizardState,
  PricelistWizardSummary,
  QuantityThreshold,
} from '../../core/pricelist.models';
import { PricelistWizardService } from '../../core/pricelist-wizard.service';
import { Region } from '../../core/region.model';
import { RegionService } from '../../core/region.service';
import { TeamService } from '../../core/team.service';
import { PricelistTeam } from '../../core/team.models';
import { ERROR_MESSAGE_MS, SUCCESS_MESSAGE_MS, TransientMessageService } from '../../core/transient-message.service';
import { PricelistWizardBasicInfoStepComponent } from './pricelist-wizard-basic-info-step.component';
import { PricelistWizardItemsStepComponent } from './pricelist-wizard-items-step.component';
import { PricelistWizardReviewStepComponent } from './pricelist-wizard-review-step.component';
import { PricelistWizardTeamAccessStepComponent } from './pricelist-wizard-team-access-step.component';
import { PricelistWizardThresholdsStepComponent } from './pricelist-wizard-thresholds-step.component';

interface WizardStepDefinition {
  id: PricelistCreationStep;
  label: string;
  description: string;
}

type SaveAction = 'draft' | 'next' | 'submit' | null;

@Component({
  selector: 'app-pricelist-create-wizard',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    PricelistWizardBasicInfoStepComponent,
    PricelistWizardTeamAccessStepComponent,
    PricelistWizardItemsStepComponent,
    PricelistWizardThresholdsStepComponent,
    PricelistWizardReviewStepComponent,
  ],
  templateUrl: './pricelist-create-wizard.component.html',
  styleUrl: './pricelist-create-wizard.component.css',
})
export class PricelistCreateWizardComponent implements OnInit, OnDestroy {
  private readonly fb = inject(UntypedFormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly regionService = inject(RegionService);
  private readonly teamService = inject(TeamService);
  private readonly portfolioService = inject(PortfolioService);
  private readonly wizardService = inject(PricelistWizardService);
  private readonly transientMessages = inject(TransientMessageService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly ngZone = inject(NgZone);

  readonly steps: WizardStepDefinition[] = [
    { id: 'BASIC_INFO', label: 'Basic information', description: 'Region, segment, currency, and validity dates' },
    { id: 'TEAM_ACCESS', label: 'Team access', description: 'Private draft or shared team ownership' },
    { id: 'ITEMS', label: 'Items', description: 'Active product variants included in this pricelist' },
    { id: 'THRESHOLDS', label: 'Thresholds', description: 'Quantity breaks and prices for each item' },
    { id: 'REVIEW', label: 'Review', description: 'Validate and submit the draft for review' },
  ];

  readonly minPeriodStart = PricelistCreateWizardComponent.todayStartInputValue();

  readonly basicInfoForm = this.fb.group(
    {
      regionId: new UntypedFormControl(null, [Validators.required]),
      customerSegment: new UntypedFormControl('', [Validators.required, Validators.maxLength(120)]),
      currency: new UntypedFormControl('RSD', [Validators.required, Validators.pattern(/^[A-Z]{3}$/)]),
      periodStart: new UntypedFormControl('', [Validators.required]),
      periodEnd: new UntypedFormControl('', [Validators.required]),
    },
    { validators: [PricelistCreateWizardComponent.periodValidator] }
  );

  readonly teamAccessForm = this.fb.group(
    {
      accessMode: new UntypedFormControl('PRIVATE'),
      teamId: new UntypedFormControl(null),
    },
    { validators: [PricelistCreateWizardComponent.teamAccessValidator] }
  );

  readonly itemsForm = this.fb.group({
    items: this.fb.array([this.createItemGroup()]),
  });

  readonly thresholdsForm = this.fb.group({
    items: this.fb.array([]),
  });

  activeStepIndex = 0;
  wizardId: number | null = null;
  state: PricelistWizardState | null = null;
  summary: PricelistWizardSummary | null = null;

  wizardLoading = true;
  lookupLoading = false;
  wizardReady = false;
  starting = false;
  saveAction: SaveAction = null;
  summaryLoading = false;
  loadError: string | null = null;
  errorMessage = '';
  successMessage = '';
  lookupWarningMessages: string[] = [];
  private summaryLoadedForWizardId: number | null = null;
  private summaryLoadingForWizardId: number | null = null;
  private routeSubscription?: Subscription;

  regions: Region[] = [];
  teams: PricelistTeam[] = [];
  categories: Category[] = [];
  availableVariants: Variant[] = [];
  subcategoriesByItem: Record<number, Subcategory[]> = {};
  productsByItem: Record<number, Product[]> = {};
  variantsByItem: Record<number, Variant[]> = {};

  ngOnInit(): void {
    this.loadLookups();
    this.routeSubscription = this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        const wizardId = Number(id);
        if (!Number.isFinite(wizardId) || wizardId <= 0) {
          this.wizardId = null;
          this.wizardReady = false;
          this.showError('Pricelist draft could not be found.');
          this.showLoadErrorMessage('Pricelist draft could not be found.');
          return;
        }
        if (this.wizardReady && this.wizardId === wizardId && this.state?.pricelistId === wizardId) {
          return;
        }
        this.wizardId = wizardId;
        this.loadWizardState(wizardId);
        return;
      }
      this.wizardId = null;
      this.state = null;
      this.summary = null;
      this.summaryLoadedForWizardId = null;
      this.wizardReady = false;
      this.requestWizardRender();
      this.startWizard();
    });
  }

  ngOnDestroy(): void {
    this.routeSubscription?.unsubscribe();
    this.transientMessages.clearAll(this);
  }

  get activeStep(): WizardStepDefinition {
    return this.steps[this.activeStepIndex] ?? this.steps[0];
  }

  get items(): UntypedFormArray {
    return this.itemsForm.get('items') as UntypedFormArray;
  }

  get thresholdItems(): UntypedFormArray {
    return this.thresholdsForm.get('items') as UntypedFormArray;
  }

  get duplicateVariantMessage(): string {
    return this.hasDuplicateVariants() ? 'A variant can only be selected once in this pricelist.' : '';
  }

  get stepSaving(): boolean {
    return this.saveAction === 'draft' || this.saveAction === 'next';
  }

  get submitting(): boolean {
    return this.saveAction === 'submit';
  }

  retryLoad(): void {
    this.clearResultMessages();
    this.clearLoadError();
    this.wizardReady = false;
    this.requestWizardRender();
    if (this.wizardId) {
      this.loadWizardState(this.wizardId);
      return;
    }
    this.startWizard();
  }

  goBack(): void {
    if (this.activeStepIndex > 0) {
      this.clearResultMessages();
      this.activeStepIndex -= 1;
      this.onActiveStepEntered(false);
    }
  }

  goToStep(stepIndex: number): void {
    if (stepIndex < 0 || stepIndex >= this.steps.length || this.wizardLoading || this.saveAction) {
      return;
    }
    if (!this.canAccessStep(stepIndex)) {
      this.showError('Complete the previous step before continuing.');
      return;
    }
    this.clearResultMessages();
    this.activeStepIndex = stepIndex;
    this.onActiveStepEntered(false);
  }

  saveCurrentStep(advance: boolean): void {
    if (!this.wizardId || this.saveAction) {
      return;
    }

    this.clearResultMessages();

    switch (this.activeStep.id) {
      case 'BASIC_INFO':
        this.saveBasicInfo(advance);
        break;
      case 'TEAM_ACCESS':
        this.saveTeamAccess(advance);
        break;
      case 'ITEMS':
        this.saveItems(advance);
        break;
      case 'THRESHOLDS':
        this.saveThresholds(advance);
        break;
      case 'REVIEW':
        this.loadSummary(true);
        break;
    }
  }

  finish(): void {
    if (this.submitDisabled()) {
      return;
    }
    this.saveAction = 'submit';
    this.clearResultMessages();
    this.wizardService.finishWizard(this.wizardId!).pipe(finalize(() => {
      this.saveAction = null;
      this.requestWizardRender();
    })).subscribe({
      next: () => {
        this.router.navigate(['/content/mine'], {
          state: { successMessage: 'Pricelist was submitted for review.' },
        });
      },
      error: (error: HttpErrorResponse) => {
        this.showError(this.createErrorMessage(error));
      },
    });
  }

  addItem(): void {
    this.items.push(this.createItemGroup());
    this.syncThresholdForms();
  }

  removeItem(index: number): void {
    if (this.items.length === 1) {
      return;
    }
    this.items.removeAt(index);
    delete this.subcategoriesByItem[index];
    delete this.productsByItem[index];
    delete this.variantsByItem[index];
    this.reindexLookupCaches(index);
    this.syncThresholdForms();
  }

  onCategoryChange(itemIndex: number): void {
    const item = this.itemGroup(itemIndex);
    const categoryId = item.controls['categoryId'].value;
    item.patchValue({
      subcategoryId: null,
      productId: null,
      variantId: null,
      existingVariantName: '',
    });
    this.productsByItem[itemIndex] = [];
    this.variantsByItem[itemIndex] = [];
    if (!categoryId) {
      this.subcategoriesByItem[itemIndex] = [];
      return;
    }
    this.portfolioService.getSubcategories(categoryId).pipe(timeout(30000)).subscribe({
      next: (subcategories) => {
        this.subcategoriesByItem[itemIndex] = subcategories.filter((subcategory) => subcategory.status === 'ACTIVE');
      },
      error: () => {
        this.subcategoriesByItem[itemIndex] = [];
        this.addLookupWarning('Subcategories could not be loaded for the selected category.');
      },
    });
  }

  onSubcategoryChange(itemIndex: number): void {
    const item = this.itemGroup(itemIndex);
    const subcategoryId = item.controls['subcategoryId'].value;
    item.patchValue({
      productId: null,
      variantId: null,
      existingVariantName: '',
    });
    this.variantsByItem[itemIndex] = [];
    if (!subcategoryId) {
      this.productsByItem[itemIndex] = [];
      return;
    }
    this.portfolioService.getProducts(subcategoryId).pipe(timeout(30000)).subscribe({
      next: (products) => {
        this.productsByItem[itemIndex] = products.filter((product) => product.status === 'ACTIVE');
      },
      error: () => {
        this.productsByItem[itemIndex] = [];
        this.addLookupWarning('Products could not be loaded for the selected subcategory.');
      },
    });
  }

  onProductChange(itemIndex: number): void {
    const item = this.itemGroup(itemIndex);
    const productId = item.controls['productId'].value;
    item.patchValue({
      variantId: null,
      existingVariantName: '',
    });
    if (!productId) {
      this.variantsByItem[itemIndex] = [];
      return;
    }
    this.variantsByItem[itemIndex] = this.availableVariants.filter((variant) => variant.productId === Number(productId));
  }

  addThreshold(itemIndex: number): void {
    this.thresholdsFor(itemIndex).push(this.createThresholdGroup());
  }

  removeThreshold(event: { itemIndex: number; thresholdIndex: number }): void {
    const thresholds = this.thresholdsFor(event.itemIndex);
    if (thresholds.length === 1) {
      return;
    }
    thresholds.removeAt(event.thresholdIndex);
  }

  itemGroup(index: number): UntypedFormGroup {
    return this.items.at(index) as UntypedFormGroup;
  }

  thresholdsFor(itemIndex: number): UntypedFormArray {
    return (this.thresholdItems.at(itemIndex) as UntypedFormGroup).get('thresholds') as UntypedFormArray;
  }

  stepClass(index: number): string {
    if (!this.canAccessStep(index)) {
      return 'disabled';
    }
    if (index === this.activeStepIndex) {
      return 'active';
    }
    return index < this.highestAccessibleStepIndex() ? 'complete' : '';
  }

  canAccessStep(index: number): boolean {
    return index >= 0 && index <= this.highestAccessibleStepIndex();
  }

  stepActionDisabled(): boolean {
    if (this.saveAction || !this.canAccessStep(this.activeStepIndex)) {
      return true;
    }

    switch (this.activeStep.id) {
      case 'BASIC_INFO':
        return this.basicInfoForm.invalid;
      case 'TEAM_ACCESS':
        return this.teamAccessForm.invalid;
      case 'ITEMS':
        return this.itemsForm.invalid || this.hasDuplicateVariants();
      case 'THRESHOLDS':
        return this.thresholdsForm.invalid || this.thresholdItems.length === 0;
      default:
        return false;
    }
  }

  submitDisabled(): boolean {
    return !this.wizardId
      || this.activeStep.id !== 'REVIEW'
      || this.summaryLoading
      || !this.summary
      || this.summary.readyToFinish !== true
      || this.saveAction !== null;
  }

  private startWizard(): void {
    if (this.starting || this.wizardId) {
      return;
    }
    this.starting = true;
    this.wizardLoading = true;
    this.wizardReady = false;
    this.clearLoadError();
    this.clearError();
    this.requestWizardRender();
    this.wizardService.startWizard().pipe(timeout(30000), finalize(() => {
      this.starting = false;
      this.wizardLoading = false;
      this.requestWizardRender();
    })).subscribe({
      next: (response) => {
        const normalized = this.normalizeStartResponse(response);
        const wizardId = normalized.pricelistId;
        if (!wizardId) {
          this.showLoadErrorMessage('Wizard was started, but the draft identifier was missing.');
          return;
        }
        this.wizardId = wizardId;
        this.requestWizardRender();
        void this.router.navigate(['/pricelists/create', wizardId], { replaceUrl: true }).then((navigated) => {
          if (!navigated) {
            this.loadWizardState(wizardId);
          }
        });
      },
      error: (error: HttpErrorResponse) => {
        this.showLoadError(error);
      },
    });
  }

  private loadWizardState(id: number): void {
    if (this.wizardReady && this.wizardId === id && this.state?.pricelistId === id) {
      return;
    }
    this.wizardId = id;
    this.wizardLoading = true;
    this.wizardReady = false;
    this.starting = false;
    this.clearLoadError();
    this.clearError();
    this.requestWizardRender();
    this.wizardService.getWizardState(id).pipe(timeout(30000), finalize(() => {
      this.wizardLoading = false;
      this.requestWizardRender();
    })).subscribe({
      next: (response) => {
        const state = this.normalizeWizardState(response) ?? this.createFallbackWizardState(id, response);
        this.finishWizardLoad(state);
      },
      error: (error: HttpErrorResponse) => {
        this.showLoadError(error);
      },
    });
  }

  private applyState(state: PricelistWizardState, followBackendStep: boolean): void {
    this.state = state;
    this.wizardId = state.pricelistId ?? this.wizardId;
    this.populateForms(state);
    if (followBackendStep) {
      this.setActiveStep(this.resolveCreationStep(state), state.status);
    }
  }

  private finishWizardLoad(state: PricelistWizardState): void {
    this.ngZone.run(() => {
      let applied = false;
      try {
        this.applyState(state, true);
        this.wizardReady = true;
        this.wizardLoading = false;
        this.loadError = null;
        applied = true;
      } catch (error) {
        console.error('Wizard data was loaded but could not be rendered.', error);
        this.wizardReady = false;
        this.wizardLoading = false;
        this.loadError = 'Wizard data was loaded but could not be rendered.';
      } finally {
        this.starting = false;
        this.requestWizardRender();
      }

      if (applied && this.wizardReady) {
        try {
          this.onActiveStepEntered(false);
        } catch (error) {
          console.error('Wizard step entry failed after state load.', error);
          this.showError('Wizard step data could not be refreshed. Try reloading the draft.');
        } finally {
          this.requestWizardRender();
        }
      }
    });
  }

  private resolveCreationStep(state: PricelistWizardState): PricelistCreationStep {
    const step = state.currentStep ?? state.creationStep ?? state.pricelist?.creationStep;
    if (step && (this.steps.some((candidate) => candidate.id === step) || step === 'COMPLETED')) {
      return step;
    }
    return 'BASIC_INFO';
  }

  private setActiveStep(step: PricelistCreationStep, status: Pricelist['status'] | null | undefined): void {
    if (step === 'COMPLETED') {
      if (status === 'DRAFT') {
        this.activeStepIndex = this.steps.findIndex((candidate) => candidate.id === 'REVIEW');
        return;
      }
      this.router.navigate(['/content/mine']);
      return;
    }
    const index = this.steps.findIndex((candidate) => candidate.id === step);
    this.activeStepIndex = this.clampStepIndex(index >= 0 ? index : 0);
  }

  private saveBasicInfo(advance: boolean): void {
    if (this.basicInfoForm.invalid) {
      this.basicInfoForm.markAllAsTouched();
      return;
    }
    const raw = this.basicInfoForm.getRawValue();
    this.saveStep(
      this.wizardService.saveBasicInfo(this.wizardId!, {
        regionId: Number(raw.regionId),
        customerSegment: raw.customerSegment.trim(),
        currency: raw.currency.trim().toUpperCase(),
        periodStart: this.toOffsetDateTime(raw.periodStart),
        periodEnd: this.toOffsetDateTime(raw.periodEnd),
      }),
      advance
    );
  }

  private saveTeamAccess(advance: boolean): void {
    if (this.teamAccessForm.invalid) {
      this.teamAccessForm.markAllAsTouched();
      return;
    }
    const raw = this.teamAccessForm.getRawValue();
    this.saveStep(
      this.wizardService.saveTeamAccess(this.wizardId!, {
        teamId: raw.accessMode === 'TEAM' ? Number(raw.teamId) : null,
      }),
      advance
    );
  }

  private saveItems(advance: boolean): void {
    if (this.itemsForm.invalid || this.hasDuplicateVariants()) {
      this.itemsForm.markAllAsTouched();
      return;
    }
    this.saveStep(
      this.wizardService.saveItems(this.wizardId!, {
        items: this.items.getRawValue().map((item: any) => ({
          variantId: Number(item.variantId),
          variantName: this.variantNameFor(item),
        })),
      }),
      advance
    );
  }

  private saveThresholds(advance: boolean): void {
    if (this.thresholdsForm.invalid || this.thresholdItems.length === 0) {
      this.thresholdsForm.markAllAsTouched();
      this.showError(this.firstThresholdValidationMessage() || 'Review threshold validation messages before continuing.');
      return;
    }
    this.saveStep(
      this.wizardService.saveThresholds(this.wizardId!, {
        items: this.thresholdItems.getRawValue().map((item: any) => ({
          variantId: Number(item.variantId),
          thresholds: item.thresholds.map((threshold: any) => ({
            quantityFrom: Number(threshold.quantityFrom),
            quantityTo: threshold.quantityTo == null || threshold.quantityTo === '' ? null : Number(threshold.quantityTo),
            price: Number(threshold.price),
          })),
        })),
      }),
      advance
    );
  }

  private saveStep(request: Observable<PricelistWizardState>, advance: boolean): void {
    const startedFromIndex = this.activeStepIndex;
    this.saveAction = advance ? 'next' : 'draft';
    request.pipe(finalize(() => {
      this.saveAction = null;
      this.requestWizardRender();
    })).subscribe({
      next: (state) => {
        this.showSuccess('Step saved.');
        this.applyState(state, false);
        if (advance) {
          this.activeStepIndex = this.clampStepIndex(startedFromIndex + 1);
          this.onActiveStepEntered(false);
        }
        this.summary = null;
        this.summaryLoadedForWizardId = null;
      },
      error: (error: HttpErrorResponse) => {
        this.showError(this.createErrorMessage(error));
      },
    });
  }

  private loadSummary(force = false): void {
    if (!this.wizardId) {
      return;
    }
    if (this.summaryLoading && this.summaryLoadingForWizardId === this.wizardId) {
      return;
    }
    if (!force && this.summaryLoadedForWizardId === this.wizardId && this.summary) {
      return;
    }
    const wizardId = this.wizardId;
    this.summaryLoading = true;
    this.summaryLoadingForWizardId = wizardId;
    this.summary = null;
    this.wizardService.getSummary(wizardId).pipe(
      timeout(30000),
      finalize(() => {
        this.summaryLoading = false;
        this.summaryLoadingForWizardId = null;
        this.requestWizardRender();
      })
    ).subscribe({
      next: (summary) => {
        this.summary = this.normalizeSummary(summary, wizardId);
        this.summaryLoadedForWizardId = wizardId;
      },
      error: (error: HttpErrorResponse) => {
        this.summaryLoadedForWizardId = null;
        this.showError(this.createErrorMessage(error));
      },
    });
  }

  private populateForms(state: PricelistWizardState): void {
    const pricelist = state.pricelist;
    if (pricelist) {
      this.basicInfoForm.patchValue({
        regionId: pricelist.regionId ?? null,
        customerSegment: this.placeholderSegment(pricelist.customerSegment) ? '' : pricelist.customerSegment,
        currency: pricelist.currency ?? 'RSD',
        periodStart: this.toDatetimeLocal(pricelist.periodStart),
        periodEnd: this.toDatetimeLocal(pricelist.periodEnd),
      });
      this.teamAccessForm.patchValue({
        accessMode: state.teamId ? 'TEAM' : 'PRIVATE',
        teamId: state.teamId ?? null,
      });
      this.populateItems(pricelist.items ?? []);
      this.populateThresholds(pricelist.items ?? []);
      return;
    }
    this.syncThresholdForms();
  }

  private populateItems(items: PricelistItem[]): void {
    this.items.clear();
    for (const item of items) {
      this.items.push(this.createItemGroup(item));
    }
    if (this.items.length === 0) {
      this.items.push(this.createItemGroup());
    }
  }

  private populateThresholds(items: PricelistItem[]): void {
    this.thresholdItems.clear();
    for (const item of items) {
      this.thresholdItems.push(this.createThresholdItemGroup(item));
    }
  }

  private syncThresholdForms(): void {
    const existingByVariantId = new Map<number, QuantityThreshold[]>();
    for (const control of this.thresholdItems.controls) {
      const group = control as UntypedFormGroup;
      const variantId = Number(group.controls['variantId'].value);
      existingByVariantId.set(variantId, group.controls['thresholds'].value);
    }

    this.thresholdItems.clear();
    for (const item of this.items.getRawValue()) {
      if (!item.variantId) {
        continue;
      }
      this.thresholdItems.push(this.createThresholdItemGroup({
        variantId: Number(item.variantId),
        variantName: this.variantNameFor(item),
        activeVariant: true,
        replacementRequired: false,
        catalogAvailable: true,
        thresholds: existingByVariantId.get(Number(item.variantId)) ?? [],
      }));
    }
  }

  private createItemGroup(item?: PricelistItem): UntypedFormGroup {
    return this.fb.group({
      categoryId: new UntypedFormControl(null),
      subcategoryId: new UntypedFormControl(null),
      productId: new UntypedFormControl(null),
      variantId: new UntypedFormControl(item?.variantId ?? null, [Validators.required]),
      existingVariantName: new UntypedFormControl(item?.variantName ?? ''),
    });
  }

  private createThresholdItemGroup(item: PricelistItem): UntypedFormGroup {
    const thresholds = item.thresholds?.length ? item.thresholds : [undefined];
    return this.fb.group({
      variantId: new UntypedFormControl(item.variantId, [Validators.required]),
      variantName: new UntypedFormControl(item.variantName),
      thresholds: this.fb.array(
        thresholds.map((threshold) => this.createThresholdGroup(threshold)),
        [PricelistCreateWizardComponent.thresholdRangesValidator]
      ),
    });
  }

  private createThresholdGroup(threshold?: QuantityThreshold): UntypedFormGroup {
    return this.fb.group({
      quantityFrom: new UntypedFormControl(threshold?.quantityFrom ?? 1, [Validators.required, Validators.min(1)]),
      quantityTo: new UntypedFormControl(threshold?.quantityTo ?? null, [Validators.min(1)]),
      price: new UntypedFormControl(threshold?.price ?? null, [Validators.required, Validators.min(0.01)]),
    });
  }

  private variantNameFor(item: any): string {
    const availableMatch = this.availableVariants.find((variant) => variant.id === Number(item.variantId));
    if (availableMatch) {
      return `${availableMatch.productName} ${availableMatch.form} ${availableMatch.dosage}`.trim();
    }
    for (const variants of Object.values(this.variantsByItem)) {
      const match = variants.find((variant) => variant.id === Number(item.variantId));
      if (match) {
        return `${match.productName} ${match.form} ${match.dosage}`.trim();
      }
    }
    return item.existingVariantName || `Variant ${item.variantId}`;
  }

  private hasDuplicateVariants(): boolean {
    const ids = this.items.getRawValue()
      .map((item: any) => Number(item.variantId))
      .filter((id: number) => Number.isFinite(id) && id > 0);
    return new Set(ids).size !== ids.length;
  }

  private loadLookups(): void {
    this.lookupLoading = true;
    forkJoin({
      regions: this.regionService.list().pipe(
        timeout(30000),
        catchError(() => {
          this.addLookupWarning('Regions could not be loaded. Region selection is temporarily unavailable.');
          return of([] as Region[]);
        })
      ),
      teams: this.teamService.getMyTeams().pipe(
        timeout(30000),
        catchError(() => {
          this.addLookupWarning('Teams could not be loaded. Team sharing is temporarily unavailable.');
          return of([] as PricelistTeam[]);
        })
      ),
      categories: this.portfolioService.getCategories().pipe(
        timeout(30000),
        map((categories) => categories.filter((category) => category.status === 'ACTIVE')),
        catchError(() => {
          this.addLookupWarning('Catalog categories could not be loaded. Item filtering is temporarily unavailable.');
          return of([] as Category[]);
        })
      ),
      variants: this.portfolioService.getVariants().pipe(
        timeout(30000),
        map((variants) => variants.filter((variant) => variant.status === 'ACTIVE')),
        catchError(() => {
          this.addLookupWarning('Active variants could not be loaded. Item selection is temporarily unavailable.');
          return of([] as Variant[]);
        })
      ),
    }).pipe(finalize(() => (this.lookupLoading = false))).subscribe(({ regions, teams, categories, variants }) => {
      this.regions = regions;
      this.teams = teams;
      this.categories = categories;
      this.availableVariants = variants;
      this.rebuildVariantFilters();
    });
  }

  private onActiveStepEntered(forceSummary: boolean): void {
    if (this.activeStep.id === 'THRESHOLDS') {
      this.syncThresholdForms();
    }
    if (this.activeStep.id === 'REVIEW') {
      this.loadSummary(forceSummary);
    }
  }

  private rebuildVariantFilters(): void {
    for (let index = 0; index < this.items.length; index++) {
      const productId = Number(this.itemGroup(index).controls['productId'].value);
      if (productId) {
        this.variantsByItem[index] = this.availableVariants.filter((variant) => variant.productId === productId);
      }
    }
  }

  private reindexLookupCaches(removedIndex: number): void {
    this.subcategoriesByItem = this.shiftLookupCache(this.subcategoriesByItem, removedIndex);
    this.productsByItem = this.shiftLookupCache(this.productsByItem, removedIndex);
    this.variantsByItem = this.shiftLookupCache(this.variantsByItem, removedIndex);
  }

  private shiftLookupCache<T>(cache: Record<number, T[]>, removedIndex: number): Record<number, T[]> {
    const next: Record<number, T[]> = {};
    for (const [key, value] of Object.entries(cache)) {
      const index = Number(key);
      if (index < removedIndex) {
        next[index] = value;
      } else if (index > removedIndex) {
        next[index - 1] = value;
      }
    }
    return next;
  }

  private normalizeStartResponse(response: unknown): { pricelistId: number | null; state: PricelistWizardState | null } {
    const root = this.objectValue(response);
    const data = this.objectValue(root?.['data']);
    const content = this.objectValue(root?.['content']);
    const state = this.normalizeWizardState(root?.['state'] ?? data?.['state'] ?? content?.['state']);
    const rawId = root?.['pricelistId'] ?? data?.['pricelistId'] ?? content?.['pricelistId'] ?? root?.['id'] ?? state?.pricelistId;
    const pricelistId = this.toPositiveNumber(rawId);
    return { pricelistId, state };
  }

  private normalizeWizardState(response: unknown): PricelistWizardState | null {
    const root = this.objectValue(response);
    const candidate = this.objectValue(root?.['data'])
      ?? this.objectValue(root?.['content'])
      ?? this.objectValue(root?.['state'])
      ?? root;

    if (!candidate) {
      return null;
    }

    const pricelist = this.objectValue(candidate['pricelist']);
    const candidateId = this.toPositiveNumber(candidate['pricelistId']) ?? this.toPositiveNumber(candidate['id']);
    if (candidateId && (candidate['creationStep'] || candidate['currentStep'] || pricelist)) {
      return {
        pricelistId: candidateId,
        currentStep: this.normalizeStep(candidate['currentStep']),
        creationStep: this.normalizeStep(candidate['creationStep'] ?? pricelist?.['creationStep']) ?? 'BASIC_INFO',
        creationCompleted: candidate['creationCompleted'] === true || pricelist?.['creationCompleted'] === true,
        status: this.normalizeStatus(candidate['status'] ?? pricelist?.['status']),
        teamId: this.toPositiveNumber(candidate['teamId'] ?? pricelist?.['teamId']),
        teamName: this.stringOrNull(candidate['teamName'] ?? pricelist?.['teamName']),
        lastEditedAt: this.stringOrNull(candidate['lastEditedAt'] ?? pricelist?.['lastEditedAt']),
        pricelist: (pricelist as Pricelist | null) ?? null,
      };
    }

    const directPricelistId = this.toPositiveNumber(candidate['id']);
    if (directPricelistId && candidate['status']) {
      const directPricelist = candidate as unknown as Pricelist;
      return {
        pricelistId: directPricelistId,
        currentStep: null,
        creationStep: this.normalizeStep(directPricelist.creationStep) ?? 'BASIC_INFO',
        creationCompleted: directPricelist.creationCompleted === true,
        status: this.normalizeStatus(directPricelist.status),
        teamId: this.toPositiveNumber(directPricelist.teamId),
        teamName: this.stringOrNull(directPricelist.teamName),
        lastEditedAt: this.stringOrNull(directPricelist.lastEditedAt),
        pricelist: directPricelist,
      };
    }

    return null;
  }

  private normalizeSummary(response: unknown, fallbackId: number): PricelistWizardSummary {
    const root = this.objectValue(response);
    const candidate = this.objectValue(root?.['data'])
      ?? this.objectValue(root?.['content'])
      ?? this.objectValue(root?.['summary'])
      ?? root;

    const validationMessages = Array.isArray(candidate?.['validationMessages'])
      ? candidate['validationMessages'].map((message) => String(message))
      : [];
    const pricelist = this.objectValue(candidate?.['pricelist']) as Pricelist | null;
    const readyToFinish = typeof candidate?.['readyToFinish'] === 'boolean'
      ? candidate['readyToFinish']
      : validationMessages.length === 0;

    return {
      pricelistId: this.toPositiveNumber(candidate?.['pricelistId']) ?? fallbackId,
      readyToFinish,
      validationMessages,
      pricelist,
    };
  }

  private createFallbackWizardState(id: number, response: unknown): PricelistWizardState {
    const pricelist = this.extractPricelist(response);
    return {
      pricelistId: id,
      currentStep: null,
      creationStep: this.normalizeStep(pricelist?.creationStep) ?? 'BASIC_INFO',
      creationCompleted: pricelist?.creationCompleted === true,
      status: this.normalizeStatus(pricelist?.status),
      teamId: this.toPositiveNumber(pricelist?.teamId),
      teamName: this.stringOrNull(pricelist?.teamName),
      lastEditedAt: this.stringOrNull(pricelist?.lastEditedAt),
      pricelist,
    };
  }

  private extractPricelist(response: unknown): Pricelist | null {
    const root = this.objectValue(response);
    const data = this.objectValue(root?.['data']);
    const content = this.objectValue(root?.['content']);
    const state = this.objectValue(root?.['state'] ?? data?.['state'] ?? content?.['state']);
    const explicitPricelist = this.objectValue(root?.['pricelist'])
      ?? this.objectValue(data?.['pricelist'])
      ?? this.objectValue(content?.['pricelist'])
      ?? this.objectValue(state?.['pricelist']);

    if (explicitPricelist) {
      return explicitPricelist as unknown as Pricelist;
    }

    const candidate = data ?? content ?? state ?? root;
    if (candidate && (candidate['status'] || candidate['regionId'] || candidate['periodStart'] || candidate['items'])) {
      return candidate as unknown as Pricelist;
    }

    return null;
  }

  private objectValue(value: unknown): Record<string, unknown> | null {
    return value && typeof value === 'object' ? value as Record<string, unknown> : null;
  }

  private toPositiveNumber(value: unknown): number | null {
    const numeric = Number(value);
    return Number.isFinite(numeric) && numeric > 0 ? numeric : null;
  }

  private stringOrNull(value: unknown): string | null {
    return typeof value === 'string' && value.trim() ? value : null;
  }

  private normalizeStep(value: unknown): PricelistCreationStep | null {
    return typeof value === 'string' && this.steps.some((step) => step.id === value || value === 'COMPLETED')
      ? value as PricelistCreationStep
      : null;
  }

  private normalizeStatus(value: unknown): Pricelist['status'] {
    return value === 'IN_REVIEW' || value === 'ACTIVE' || value === 'ARCHIVED' ? value : 'DRAFT';
  }

  private highestAccessibleStepIndex(): number {
    const derived = this.derivedAccessibleStepIndex();
    const backend = this.backendAccessibleStepIndex();
    return Math.max(0, Math.min(this.steps.length - 1, Math.max(derived, backend)));
  }

  private backendAccessibleStepIndex(): number {
    const step = this.resolveCreationStep(this.state ?? this.createFormDerivedState());
    if (step === 'COMPLETED') {
      return this.state?.status === 'DRAFT' ? this.steps.length - 1 : 0;
    }
    const index = this.steps.findIndex((candidate) => candidate.id === step);
    return index >= 0 ? index : 0;
  }

  private derivedAccessibleStepIndex(): number {
    let highest = 0;
    if (!this.basicInfoCompleted()) {
      return highest;
    }
    highest = 1;
    if (!this.teamAccessCompleted()) {
      return highest;
    }
    highest = 2;
    if (!this.itemsCompleted()) {
      return highest;
    }
    highest = 3;
    if (this.thresholdsCompleted()) {
      highest = 4;
    }
    return highest;
  }

  private clampStepIndex(index: number): number {
    return Math.max(0, Math.min(index, this.highestAccessibleStepIndex(), this.steps.length - 1));
  }

  private createFormDerivedState(): PricelistWizardState {
    return {
      pricelistId: this.wizardId ?? 0,
      currentStep: null,
      creationStep: 'BASIC_INFO',
      creationCompleted: false,
      status: 'DRAFT',
      teamId: this.toPositiveNumber(this.teamAccessForm.get('teamId')?.value),
      teamName: null,
      lastEditedAt: null,
      pricelist: null,
    };
  }

  private basicInfoCompleted(): boolean {
    const pricelist = this.state?.pricelist;
    const regionId = this.toPositiveNumber(pricelist?.regionId ?? this.basicInfoForm.get('regionId')?.value);
    const customerSegment = String(pricelist?.customerSegment ?? this.basicInfoForm.get('customerSegment')?.value ?? '').trim();
    const currency = String(pricelist?.currency ?? this.basicInfoForm.get('currency')?.value ?? '').trim();
    const periodStart = String(pricelist?.periodStart ?? this.basicInfoForm.get('periodStart')?.value ?? '').trim();
    const periodEnd = String(pricelist?.periodEnd ?? this.basicInfoForm.get('periodEnd')?.value ?? '').trim();
    return !!regionId
      && !this.placeholderSegment(customerSegment)
      && /^[A-Z]{3}$/.test(currency)
      && !!periodStart
      && !!periodEnd
      && new Date(periodStart).getTime() < new Date(periodEnd).getTime();
  }

  private teamAccessCompleted(): boolean {
    const backendStep = this.state?.currentStep ?? this.state?.creationStep;
    if (backendStep === 'ITEMS' || backendStep === 'THRESHOLDS' || backendStep === 'REVIEW' || backendStep === 'COMPLETED' || this.state?.creationCompleted) {
      return true;
    }
    return !!this.state?.teamId;
  }

  private itemsCompleted(): boolean {
    const items = this.state?.pricelist?.items ?? [];
    return items.some((item) => Number.isFinite(Number(item.variantId)) && Number(item.variantId) > 0);
  }

  private thresholdsCompleted(): boolean {
    const items = this.state?.pricelist?.items ?? [];
    return items.length > 0 && items.every((item) => this.thresholdsAreComplete(item.thresholds ?? []));
  }

  private thresholdsAreComplete(thresholds: QuantityThreshold[]): boolean {
    if (!thresholds.length) {
      return false;
    }
    const normalized = thresholds.map((threshold) => ({
      quantityFrom: Number(threshold.quantityFrom),
      quantityTo: threshold.quantityTo == null ? null : Number(threshold.quantityTo),
      price: Number(threshold.price),
    })).sort((first, second) => first.quantityFrom - second.quantityFrom);
    for (let index = 0; index < normalized.length; index++) {
      const current = normalized[index];
      const previous = index > 0 ? normalized[index - 1] : null;
      if (!Number.isFinite(current.quantityFrom) || current.quantityFrom < 1 || !Number.isFinite(current.price) || current.price <= 0) {
        return false;
      }
      if (current.quantityTo != null && (!Number.isFinite(current.quantityTo) || current.quantityTo <= current.quantityFrom)) {
        return false;
      }
      if (previous) {
        if (previous.quantityTo == null || current.quantityFrom !== previous.quantityTo + 1 || current.price > previous.price) {
          return false;
        }
      } else if (current.quantityFrom !== 1) {
        return false;
      }
      if (current.quantityTo == null && index < normalized.length - 1) {
        return false;
      }
    }
    return normalized[normalized.length - 1].quantityTo == null;
  }

  private showLoadError(error: unknown): void {
    const detail = extractBackendErrorMessage(error, '').trim();
    this.showLoadErrorMessage(detail || 'Please try again.');
  }

  private showLoadErrorMessage(message: string): void {
    this.wizardLoading = false;
    this.starting = false;
    this.wizardReady = false;
    this.loadError = message;
    this.requestWizardRender();
  }

  private clearLoadError(): void {
    this.loadError = null;
    this.requestWizardRender();
  }

  private requestWizardRender(): void {
    queueMicrotask(() => {
      try {
        this.cdr.markForCheck();
        this.cdr.detectChanges();
      } catch {
        // Change detection may be skipped after route changes destroy the view.
      }
    });
  }

  private addLookupWarning(message: string): void {
    if (!this.lookupWarningMessages.includes(message)) {
      this.lookupWarningMessages = [...this.lookupWarningMessages, message];
    }
  }

  private showSuccess(message: string): void {
    this.transientMessages.setField(this, 'successMessage', message, SUCCESS_MESSAGE_MS);
  }

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS);
  }

  private clearError(): void {
    this.transientMessages.clearField(this, 'errorMessage');
  }

  private clearResultMessages(): void {
    this.transientMessages.clearField(this, 'successMessage');
    this.clearError();
  }

  private createErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 409) {
      return 'A conflict exists with an already existing pricelist.';
    }
    if (error.status === 403) {
      return 'You do not have access to this pricelist.';
    }
    return extractBackendErrorMessage(error, 'Wizard action failed. Please try again.');
  }

  private toDatetimeLocal(value: string | null | undefined): string {
    if (!value) {
      return '';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return '';
    }
    const offsetMs = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - offsetMs).toISOString().slice(0, 16);
  }

  private toOffsetDateTime(value: string): string {
    const date = new Date(value);
    const offsetMinutes = -date.getTimezoneOffset();
    const sign = offsetMinutes >= 0 ? '+' : '-';
    const absoluteOffset = Math.abs(offsetMinutes);
    const hours = String(Math.floor(absoluteOffset / 60)).padStart(2, '0');
    const minutes = String(absoluteOffset % 60).padStart(2, '0');
    const localDateTime = value.length === 16 ? `${value}:00` : value;
    return `${localDateTime}${sign}${hours}:${minutes}`;
  }

  private placeholderSegment(segment: string | null | undefined): boolean {
    return !segment || segment === 'UNDEFINED';
  }

  static periodValidator(control: AbstractControl): ValidationErrors | null {
    const periodStart = control.get('periodStart')?.value;
    const periodEnd = control.get('periodEnd')?.value;
    const errors: ValidationErrors = {};

    if (periodStart && PricelistCreateWizardComponent.isBeforeToday(periodStart)) {
      errors['periodStartInPast'] = true;
    }

    if (periodStart && periodEnd && !(new Date(periodStart) < new Date(periodEnd))) {
      errors['periodOrder'] = true;
    }

    return Object.keys(errors).length ? errors : null;
  }

  static thresholdRangesValidator(control: AbstractControl): ValidationErrors | null {
    const array = control as UntypedFormArray;
    const thresholds = array.controls.map((thresholdControl, index) => {
      const group = thresholdControl as UntypedFormGroup;
      const quantityFrom = Number(group.get('quantityFrom')?.value);
      const rawQuantityTo = group.get('quantityTo')?.value;
      const quantityTo = rawQuantityTo == null || rawQuantityTo === '' ? null : Number(rawQuantityTo);
      const price = Number(group.get('price')?.value);
      return { index, quantityFrom, quantityTo, price };
    });
    const messages: string[] = [];

    if (!thresholds.length) {
      return { thresholdRange: ['Each item must have at least one threshold.'] };
    }

    for (const threshold of thresholds) {
      const label = `Threshold ${threshold.index + 1}`;
      if (!Number.isFinite(threshold.quantityFrom) || threshold.quantityFrom < 1) {
        messages.push(`${label}: min quantity must be positive.`);
      }
      if (threshold.quantityTo != null && (!Number.isFinite(threshold.quantityTo) || threshold.quantityTo < 1)) {
        messages.push(`${label}: max quantity must be positive.`);
      }
      if (threshold.quantityTo != null && threshold.quantityTo <= threshold.quantityFrom) {
        messages.push(`${label}: max quantity must be greater than min quantity.`);
      }
      if (!Number.isFinite(threshold.price) || threshold.price <= 0) {
        messages.push(`${label}: price must be greater than zero.`);
      }
    }

    if (messages.length) {
      return { thresholdRange: messages };
    }

    const sorted = [...thresholds].sort((first, second) => first.quantityFrom - second.quantityFrom);
    for (let index = 0; index < sorted.length; index++) {
      const current = sorted[index];
      const previous = index > 0 ? sorted[index - 1] : null;

      if (previous) {
        if (previous.quantityTo == null) {
          messages.push('No threshold can follow an open-ended threshold.');
          break;
        }
        const expectedFrom = previous.quantityTo + 1;
        if (current.quantityFrom < expectedFrom) {
          messages.push('Quantity ranges cannot overlap.');
        }
        if (current.quantityFrom > expectedFrom) {
          messages.push('Quantity ranges must be continuous without gaps.');
        }
        if (current.price > previous.price) {
          messages.push('Price for a higher quantity threshold must be equal to or lower than the previous one.');
        }
      }

      if (current.quantityTo == null && index < sorted.length - 1) {
        messages.push('The open-ended threshold must be the final range.');
      }
    }

    return messages.length ? { thresholdRange: Array.from(new Set(messages)) } : null;
  }

  private firstThresholdValidationMessage(): string {
    for (const item of this.thresholdItems.controls) {
      const thresholds = (item as UntypedFormGroup).get('thresholds');
      const messages = thresholds?.errors?.['thresholdRange'];
      if (Array.isArray(messages) && messages.length) {
        return String(messages[0]);
      }
    }
    return '';
  }

  private static isBeforeToday(value: string): boolean {
    const datePart = value.slice(0, 10);
    return /^\d{4}-\d{2}-\d{2}$/.test(datePart) && datePart < PricelistCreateWizardComponent.todayDateValue();
  }

  private static todayStartInputValue(): string {
    return `${PricelistCreateWizardComponent.todayDateValue()}T00:00`;
  }

  private static todayDateValue(): string {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  static teamAccessValidator(control: AbstractControl): ValidationErrors | null {
    if (control.get('accessMode')?.value === 'TEAM' && !control.get('teamId')?.value) {
      return { teamRequired: true };
    }
    return null;
  }
}
