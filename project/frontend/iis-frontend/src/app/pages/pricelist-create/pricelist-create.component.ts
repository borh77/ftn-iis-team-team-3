import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormArray, ReactiveFormsModule, UntypedFormArray, UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Region } from '../../core/region.model';
import { RegionService } from '../../core/region.service';
import { PortfolioService } from '../../core/portfolio.service';
import { Category, Product, Subcategory, Variant } from '../../core/portfolio.models';
import { PricelistService } from '../../core/pricelist.service';
import { CreatePricelistPayload } from '../../core/pricelist.models';

type ThresholdGroup = UntypedFormGroup;
type ItemGroup = UntypedFormGroup;
type PricelistFormGroup = UntypedFormGroup;

@Component({
  selector: 'app-pricelist-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './pricelist-create.component.html',
  styleUrl: './pricelist-create.component.css',
})
export class PricelistCreateComponent implements OnInit {
  private readonly fb = inject(UntypedFormBuilder);
  private readonly regionService = inject(RegionService);
  private readonly portfolioService = inject(PortfolioService);
  private readonly pricelistService = inject(PricelistService);
  private readonly router = inject(Router);

  loadingRegions = false;
  loadingLookup = false;
  saving = false;
  errorMessage = '';
  successMessage = '';

  regions: Region[] = [];
  categories: Category[] = [];
  subcategoriesByItem: Record<number, Subcategory[]> = {};
  productsByItem: Record<number, Product[]> = {};
  variantsByItem: Record<number, Variant[]> = {};

  readonly form: PricelistFormGroup = this.fb.group(
    {
      regionId: new UntypedFormControl(null, [Validators.required]),
      customerSegment: new UntypedFormControl('', [Validators.required, Validators.maxLength(120)]),
      currency: new UntypedFormControl('RSD', [Validators.required, Validators.pattern(/^[A-Z]{3}$/)]),
      periodStart: new UntypedFormControl('', [Validators.required]),
      periodEnd: new UntypedFormControl('', [Validators.required]),
      items: this.fb.array([this.createItemGroup()]),
    },
    { validators: [PricelistCreateComponent.periodValidator] }
  );

  ngOnInit(): void {
    this.loadRegions();
    this.loadCategories();
  }

  get items(): FormArray<ItemGroup> {
    return this.form.get('items') as UntypedFormArray;
  }

  thresholdsFor(itemIndex: number): FormArray<ThresholdGroup> {
    return this.itemGroup(itemIndex).get('thresholds') as UntypedFormArray;
  }

  itemGroup(index: number): ItemGroup {
    return this.items.at(index) as ItemGroup;
  }

  addItem(): void {
    this.items.push(this.createItemGroup());
  }

  removeItem(index: number): void {
    if (this.items.length === 1) {
      return;
    }
    this.items.removeAt(index);
  }

  addThreshold(itemIndex: number): void {
    this.thresholdsFor(itemIndex).push(this.createThresholdGroup());
  }

  removeThreshold(itemIndex: number, thresholdIndex: number): void {
    const thresholds = this.thresholdsFor(itemIndex);
    if (thresholds.length === 1) {
      return;
    }
    thresholds.removeAt(thresholdIndex);
  }

  onCategoryChange(itemIndex: number): void {
    const item = this.itemGroup(itemIndex);
    const categoryId = item.controls['categoryId'].value;
    item.controls['subcategoryId'].setValue(null);
    item.controls['productId'].setValue(null);
    item.controls['variantId'].setValue(null);
    this.productsByItem[itemIndex] = [];
    this.variantsByItem[itemIndex] = [];

    if (!categoryId) {
      this.subcategoriesByItem[itemIndex] = [];
      return;
    }

    this.loadingLookup = true;
    this.portfolioService.getSubcategories(categoryId).subscribe({
      next: (subcategories) => {
        this.subcategoriesByItem[itemIndex] = subcategories;
        this.loadingLookup = false;
      },
      error: () => {
        this.subcategoriesByItem[itemIndex] = [];
        this.loadingLookup = false;
      },
    });
  }

  onSubcategoryChange(itemIndex: number): void {
    const item = this.itemGroup(itemIndex);
    const subcategoryId = item.controls['subcategoryId'].value;
    item.controls['productId'].setValue(null);
    item.controls['variantId'].setValue(null);
    this.variantsByItem[itemIndex] = [];

    if (!subcategoryId) {
      this.productsByItem[itemIndex] = [];
      return;
    }

    this.loadingLookup = true;
    this.portfolioService.getProducts(subcategoryId).subscribe({
      next: (products) => {
        this.productsByItem[itemIndex] = products;
        this.loadingLookup = false;
      },
      error: () => {
        this.productsByItem[itemIndex] = [];
        this.loadingLookup = false;
      },
    });
  }

  onProductChange(itemIndex: number): void {
    const item = this.itemGroup(itemIndex);
    const productId = item.controls['productId'].value;
    item.controls['variantId'].setValue(null);

    if (!productId) {
      this.variantsByItem[itemIndex] = [];
      return;
    }

    this.loadingLookup = true;
    this.portfolioService.getVariants(productId).subscribe({
      next: (variants) => {
        this.variantsByItem[itemIndex] = variants.filter((variant) => variant.status === 'ACTIVE');
        this.loadingLookup = false;
      },
      error: () => {
        this.variantsByItem[itemIndex] = [];
        this.loadingLookup = false;
      },
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload = this.buildPayload();
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.pricelistService.create(payload).subscribe({
      next: () => {
        this.saving = false;
        this.successMessage = 'Pricelist was successfully created in DRAFT status.';
        this.form.reset({
          regionId: null,
          customerSegment: '',
          currency: 'RSD',
          periodStart: '',
          periodEnd: '',
        });
        this.items.clear();
        this.items.push(this.createItemGroup());
        this.subcategoriesByItem = {};
        this.productsByItem = {};
        this.variantsByItem = {};
      },
      error: (error) => {
        this.saving = false;
        if (error instanceof HttpErrorResponse) {
          this.errorMessage = this.createErrorMessage(error);
        } else {
          this.errorMessage = 'Pricelist creation failed.';
        }
      },
    });
  }

  private createErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 409) {
      const backendMessage = typeof error.error?.error === 'string' ? error.error.error.trim() : '';
      return this.isEnglishMessage(backendMessage)
        ? backendMessage
        : 'A conflict exists with an already existing pricelist.';
    }
    if (error.status === 400) {
      return 'Invalid quantity thresholds. Check gaps, overlaps, and discount prices.';
    }
    if (error.status === 422) {
      return 'Some selected medicine variants do not exist or are not active.';
    }
    return 'Pricelist creation failed.';
  }

  private isEnglishMessage(message: string): boolean {
    const serbianTerms = /\b(cenovnik|postoji|pokusajte|izabrani|vec|već|periodu|pragovi|varijantu)\b/i;
    return message.length > 0 && !serbianTerms.test(message);
  }

  private loadRegions(): void {
    this.loadingRegions = true;
    this.regionService.list().subscribe({
      next: (regions) => {
        this.regions = regions;
        this.loadingRegions = false;
      },
      error: () => {
        this.regions = [];
        this.loadingRegions = false;
      },
    });
  }

  private loadCategories(): void {
    this.portfolioService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories.filter((category) => category.status === 'ACTIVE');
      },
      error: () => {
        this.categories = [];
      },
    });
  }

  private createItemGroup() {
    return this.fb.group({
      categoryId: new UntypedFormControl(null, [Validators.required]),
      subcategoryId: new UntypedFormControl(null, [Validators.required]),
      productId: new UntypedFormControl(null, [Validators.required]),
      variantId: new UntypedFormControl(null, [Validators.required]),
      thresholds: this.fb.array([this.createThresholdGroup()]),
    });
  }

  private createThresholdGroup() {
    return this.fb.group({
      quantityFrom: new UntypedFormControl(1, [Validators.required, Validators.min(1)]),
      quantityTo: new UntypedFormControl(null, [Validators.min(1)]),
      price: new UntypedFormControl(null, [Validators.required, Validators.min(0.01)]),
    });
  }

  private buildPayload(): CreatePricelistPayload {
    const raw: any = this.form.getRawValue();

    return {
      regionId: raw.regionId as number,
      customerSegment: raw.customerSegment.trim(),
      currency: raw.currency.trim().toUpperCase(),
      periodStart: new Date(raw.periodStart).toISOString(),
      periodEnd: new Date(raw.periodEnd).toISOString(),
      items: raw.items.map((item: any) => ({
        id: undefined,
        variantId: item.variantId as number,
        variantName: this.resolveVariantName(item.variantId as number),
        thresholds: item.thresholds.map((threshold: any) => ({
          quantityFrom: threshold.quantityFrom,
          quantityTo: threshold.quantityTo,
          price: threshold.price as number,
        })),
      })),
    };
  }

  private resolveVariantName(variantId: number): string {
    for (const variants of Object.values(this.variantsByItem)) {
      const match = variants.find((variant) => variant.id === variantId);
      if (match) {
        return `${match.productName} ${match.form} ${match.dosage}`.trim();
      }
    }
    return `Variant ${variantId}`;
  }

  controlError(itemIndex: number, controlName: 'categoryId' | 'subcategoryId' | 'productId' | 'variantId'): string {
    const control = this.itemGroup(itemIndex).controls[controlName];
    if (!control.touched && !control.dirty) {
      return '';
    }
    if (control.hasError('required')) {
      return 'This field is required.';
    }
    return '';
  }

  thresholdError(itemIndex: number, thresholdIndex: number, controlName: 'quantityFrom' | 'quantityTo' | 'price'): string {
    const control = this.thresholdsFor(itemIndex).at(thresholdIndex).controls[controlName];
    if (!control.touched && !control.dirty) {
      return '';
    }
    if (control.hasError('required')) {
      return 'This field is required.';
    }
    if (control.hasError('min')) {
      return controlName === 'price' ? 'Price must be greater than zero.' : 'Value must be greater than zero.';
    }
    return '';
  }

  periodError(): string {
    if (!this.form.touched && !this.form.dirty) {
      return '';
    }

    if (this.form.hasError('periodOrder')) {
      return 'Start period must be strictly before end period.';
    }

    return '';
  }

  static periodValidator(control: AbstractControl): ValidationErrors | null {
    const periodStart = control.get('periodStart')?.value;
    const periodEnd = control.get('periodEnd')?.value;

    if (!periodStart || !periodEnd) {
      return null;
    }

    return new Date(periodStart) < new Date(periodEnd) ? null : { periodOrder: true };
  }
}
