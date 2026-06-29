import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
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
import { finalize, Observable } from 'rxjs';
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

  readonly steps: WizardStepDefinition[] = [
    { id: 'BASIC_INFO', label: 'Basic information' },
    { id: 'TEAM_ACCESS', label: 'Team' },
    { id: 'ITEMS', label: 'Items' },
    { id: 'THRESHOLDS', label: 'Thresholds' },
    { id: 'REVIEW', label: 'Review' },
  ];

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

  loadingState = false;
  starting = false;
  saving = false;
  loadingSummary = false;
  errorMessage = '';
  successMessage = '';

  regions: Region[] = [];
  teams: PricelistTeam[] = [];
  categories: Category[] = [];
  subcategoriesByItem: Record<number, Subcategory[]> = {};
  productsByItem: Record<number, Product[]> = {};
  variantsByItem: Record<number, Variant[]> = {};

  ngOnInit(): void {
    this.loadLookups();
    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.wizardId = Number(id);
        this.loadWizardState(this.wizardId);
        return;
      }
      this.startWizard();
    });
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  get activeStep(): WizardStepDefinition {
    return this.steps[this.activeStepIndex];
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
    this.portfolioService.getSubcategories(categoryId).subscribe({
      next: (subcategories) => {
        this.subcategoriesByItem[itemIndex] = subcategories.filter((subcategory) => subcategory.status === 'ACTIVE');
      },
      error: () => {
        this.subcategoriesByItem[itemIndex] = [];
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
    this.portfolioService.getProducts(subcategoryId).subscribe({
      next: (products) => {
        this.productsByItem[itemIndex] = products.filter((product) => product.status === 'ACTIVE');
      },
      error: () => {
        this.productsByItem[itemIndex] = [];
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
    this.portfolioService.getVariants(productId).subscribe({
      next: (variants) => {
        this.variantsByItem[itemIndex] = variants.filter((variant) => variant.status === 'ACTIVE');
      },
      error: () => {
        this.variantsByItem[itemIndex] = [];
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

  private startWizard(): void {
    if (this.starting || this.wizardId) {
      return;
    }
    this.starting = true;
    this.loadingState = true;
    this.clearError();
    this.wizardService.startWizard().pipe(finalize(() => {
      this.starting = false;
      this.loadingState = false;
    })).subscribe({
      next: (response) => {
        this.wizardId = response.pricelistId;
        this.applyState(response.state, true);
        this.router.navigate(['/pricelists/create', response.pricelistId], { replaceUrl: true });
      },
      error: (error: HttpErrorResponse) => {
        this.showError(this.createErrorMessage(error));
      },
    });
  }

  private loadWizardState(id: number): void {
    this.loadingState = true;
    this.clearError();
    this.wizardService.getWizardState(id).pipe(finalize(() => (this.loadingState = false))).subscribe({
      next: (state) => this.applyState(state, true),
      error: (error: HttpErrorResponse) => {
        this.showError(this.createErrorMessage(error));
      },
    });
  }

  private applyState(state: PricelistWizardState, followBackendStep: boolean): void {
    this.state = state;
    this.wizardId = state.pricelistId;
    this.populateForms(state);
    if (followBackendStep) {
      this.setActiveStep(state.creationStep);
    }
    if (this.activeStep.id === 'REVIEW') {
      this.loadSummary();
    }
  }

  private setActiveStep(step: PricelistCreationStep): void {
    if (step === 'COMPLETED') {
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
        periodStart: new Date(raw.periodStart).toISOString(),
        periodEnd: new Date(raw.periodEnd).toISOString(),
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
    this.regionService.list().subscribe({
      next: (regions) => (this.regions = regions),
      error: () => (this.regions = []),
    });
    this.teamService.getMyTeams().subscribe({
      next: (teams) => (this.teams = teams),
      error: () => (this.teams = []),
    });
    this.portfolioService.getCategories().subscribe({
      next: (categories) => (this.categories = categories.filter((category) => category.status === 'ACTIVE')),
      error: () => (this.categories = []),
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
    if (typeof error.error?.error === 'string' && error.error.error.trim()) {
      return error.error.error.trim();
    }
    if (typeof error.error?.message === 'string' && error.error.message.trim()) {
      return error.error.message.trim();
    }
    if (typeof error.error?.detail === 'string' && error.error.detail.trim()) {
      return error.error.detail.trim();
    }
    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error.trim();
    }
    if (error.status === 409) {
      return 'A conflict exists with an already existing pricelist.';
    }
    if (error.status === 403) {
      return 'You do not have access to this pricelist.';
    }
    return 'Wizard action failed. Please try again.';
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

  private placeholderSegment(segment: string | null | undefined): boolean {
    return !segment || segment === 'UNDEFINED';
  }

  static periodValidator(control: AbstractControl): ValidationErrors | null {
    const periodStart = control.get('periodStart')?.value;
    const periodEnd = control.get('periodEnd')?.value;
    if (!periodStart || !periodEnd) {
      return null;
    }
    return new Date(periodStart) < new Date(periodEnd) ? null : { periodOrder: true };
  }

  static teamAccessValidator(control: AbstractControl): ValidationErrors | null {
    if (control.get('accessMode')?.value === 'TEAM' && !control.get('teamId')?.value) {
      return { teamRequired: true };
    }
    return null;
  }
}
