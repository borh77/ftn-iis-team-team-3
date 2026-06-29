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
import { finalize, Observable, Subscription, timeout } from 'rxjs';
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
    { id: 'REVIEW', label: 'Review', description: 'Validate and finish the draft wizard' },
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

  loading = true;
  wizardReady = false;
  starting = false;
  saving = false;
  loadingSummary = false;
  loadError: string | null = null;
  errorMessage = '';
  successMessage = '';
  lookupWarningMessages: string[] = [];
  private routeSubscription?: Subscription;

  regions: Region[] = [];
  teams: PricelistTeam[] = [];
  categories: Category[] = [];
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
    }
  }

  saveCurrentStep(advance: boolean): void {
    if (!this.wizardId || this.saving) {
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
        this.loadSummary();
        break;
    }
  }

  finish(): void {
    if (!this.wizardId || this.saving) {
      return;
    }
    if (!this.summary?.readyToFinish) {
      this.showError('Review the validation messages before finishing the wizard.');
      return;
    }
    this.saving = true;
    this.clearResultMessages();
    this.wizardService.finishWizard(this.wizardId).pipe(finalize(() => (this.saving = false))).subscribe({
      next: () => {
        this.router.navigate(['/content/mine'], {
          state: { successMessage: 'Pricelist creation was completed.' },
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
    this.portfolioService.getVariants(productId).pipe(timeout(30000)).subscribe({
      next: (variants) => {
        this.variantsByItem[itemIndex] = variants.filter((variant) => variant.status === 'ACTIVE');
      },
      error: () => {
        this.variantsByItem[itemIndex] = [];
        this.addLookupWarning('Variants could not be loaded for the selected product.');
      },
    });
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
    if (index === this.activeStepIndex) {
      return 'active';
    }
    return index < this.activeStepIndex ? 'complete' : '';
  }

  stepActionDisabled(): boolean {
    if (this.saving) {
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

  private startWizard(): void {
    if (this.starting || this.wizardId) {
      return;
    }
    this.starting = true;
    this.loading = true;
    this.wizardReady = false;
    this.clearLoadError();
    this.clearError();
    this.requestWizardRender();
    this.wizardService.startWizard().pipe(timeout(30000), finalize(() => {
      this.starting = false;
      this.loading = false;
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
    this.loading = true;
    this.wizardReady = false;
    this.starting = false;
    this.clearLoadError();
    this.clearError();
    this.requestWizardRender();
    this.wizardService.getWizardState(id).pipe(timeout(30000), finalize(() => {
      if (!this.loadError) {
        this.loading = false;
      }
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
    if (this.activeStep.id === 'REVIEW') {
      this.loadSummary();
    }
  }

  private finishWizardLoad(state: PricelistWizardState): void {
    this.ngZone.run(() => {
      try {
        this.applyState(state, true);
        this.wizardReady = true;
        this.loadError = null;
      } catch (error) {
        console.error('Wizard data was loaded but could not be rendered.', error);
        this.loadError = 'Wizard data was loaded but could not be rendered.';
        this.wizardReady = false;
      } finally {
        this.loading = false;
        this.starting = false;
        this.requestWizardRender();
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
    this.activeStepIndex = index >= 0 ? index : 0;
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
    this.saving = true;
    request.pipe(finalize(() => (this.saving = false))).subscribe({
      next: (state) => {
        this.showSuccess('Step saved.');
        this.applyState(state, advance);
      },
      error: (error: HttpErrorResponse) => {
        this.showError(this.createErrorMessage(error));
      },
    });
  }

  private loadSummary(): void {
    if (!this.wizardId) {
      return;
    }
    this.loadingSummary = true;
    this.summary = null;
    this.wizardService.getSummary(this.wizardId).pipe(finalize(() => (this.loadingSummary = false))).subscribe({
      next: (summary) => {
        this.summary = summary;
      },
      error: (error: HttpErrorResponse) => {
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
    const lookupValidators = item ? [] : [Validators.required];
    return this.fb.group({
      categoryId: new UntypedFormControl(null, lookupValidators),
      subcategoryId: new UntypedFormControl(null, lookupValidators),
      productId: new UntypedFormControl(null, lookupValidators),
      variantId: new UntypedFormControl(item?.variantId ?? null, [Validators.required]),
      existingVariantName: new UntypedFormControl(item?.variantName ?? ''),
    });
  }

  private createThresholdItemGroup(item: PricelistItem): UntypedFormGroup {
    const thresholds = item.thresholds?.length ? item.thresholds : [undefined];
    return this.fb.group({
      variantId: new UntypedFormControl(item.variantId, [Validators.required]),
      variantName: new UntypedFormControl(item.variantName),
      thresholds: this.fb.array(thresholds.map((threshold) => this.createThresholdGroup(threshold))),
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
    this.regionService.list().pipe(timeout(30000)).subscribe({
      next: (regions) => (this.regions = regions),
      error: () => {
        this.regions = [];
        this.addLookupWarning('Regions could not be loaded. Region selection is temporarily unavailable.');
      },
    });
    this.teamService.getMyTeams().pipe(timeout(30000)).subscribe({
      next: (teams) => (this.teams = teams),
      error: () => {
        this.teams = [];
        this.addLookupWarning('Teams could not be loaded. Team sharing is temporarily unavailable.');
      },
    });
    this.portfolioService.getCategories().pipe(timeout(30000)).subscribe({
      next: (categories) => (this.categories = categories.filter((category) => category.status === 'ACTIVE')),
      error: () => {
        this.categories = [];
        this.addLookupWarning('Catalog categories could not be loaded. Item selection is temporarily unavailable.');
      },
    });
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

  private showLoadError(error: unknown): void {
    const detail = extractBackendErrorMessage(error, '').trim();
    this.showLoadErrorMessage(detail || 'Please try again.');
  }

  private showLoadErrorMessage(message: string): void {
    this.loading = false;
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
      } catch (error) {
        console.warn('Wizard change detection skipped.', error);
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
