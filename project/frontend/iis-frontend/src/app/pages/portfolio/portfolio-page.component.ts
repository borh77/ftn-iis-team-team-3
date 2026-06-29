import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { PortfolioService } from '../../core/portfolio/portfolio.service';
import {
  IngredientResponse,
  ProductResponse,
  VariantResponse,
  VariantVersionResponse,
  VariantVersionIngredientsResponse,
  SubcategoryResponse,
  TherapeuticAreaResponse,
  MarketProductResponse,
  MarketLicenseResponse,
  MarketLicenseStatus,
  MarketLicenseHistoryResponse,
  VariantVersionLifecycleHistoryResponse,
  VariantVersionStatusCountResponse,
  ProductCountByTherapeuticAreaResponse,
  RegionResponse,
  MarketLicenseStatusCountResponse,
  MarketProductCountByRegionResponse,
} from '../../core/portfolio/portfolio.models';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../core/transient-message.service';

import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

type PortfolioTab =
  | 'products'
  | 'variants'
  | 'versions'
  | 'ingredients'
  | 'bom'
  | 'market-products'
  | 'market-licenses'
  | 'lifecycle'
  | 'analytics';

@Component({
  selector: 'app-portfolio-page',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './portfolio-page.component.html',
  styleUrl: './portfolio-page.component.css',
})
export class PortfolioPageComponent implements OnInit, OnDestroy {
  private readonly portfolioService = inject(PortfolioService);
  private readonly transientMessages = inject(TransientMessageService);

    readonly activeTab = signal<PortfolioTab>('products');

    readonly products = signal<ProductResponse[]>([]);
    readonly ingredients = signal<IngredientResponse[]>([]);
    readonly variants = signal<VariantResponse[]>([]);
    readonly versions = signal<VariantVersionResponse[]>([]);
    readonly versionIngredients = signal<VariantVersionIngredientsResponse[]>([]);

    readonly loadingVersionIngredients = signal(false);
    readonly loadingProducts = signal(false);
    readonly loadingIngredients = signal(false);
    readonly loadingVariants = signal(false);
    readonly loadingVersions = signal(false);

    readonly subcategories = signal<SubcategoryResponse[]>([]);
    readonly therapeuticAreas = signal<TherapeuticAreaResponse[]>([]);


    readonly marketProducts = signal<MarketProductResponse[]>([]);
    readonly marketLicenses = signal<MarketLicenseResponse[]>([]);
    readonly marketLicenseHistory = signal<MarketLicenseHistoryResponse[]>([]);
    readonly variantLifecycleHistory = signal<VariantVersionLifecycleHistoryResponse[]>([]);
    readonly versionStatusCounts = signal<VariantVersionStatusCountResponse[]>([]);
    readonly productsByTherapeuticArea = signal<ProductCountByTherapeuticAreaResponse[]>([]);
    readonly expiringLicenses = signal<MarketLicenseResponse[]>([]);
    readonly regions = signal<RegionResponse[]>([]);

    readonly versionHistory = signal<VariantVersionLifecycleHistoryResponse[]>([]);
    readonly loadingVersionHistory = signal(false);
    readonly selectedVersionId = signal(0);

    readonly loadingMarketProducts = signal(false);
    readonly loadingMarketLicenses = signal(false);
    readonly loadingMarketLicenseHistory = signal(false);
    readonly loadingVariantLifecycleHistory = signal(false);
    readonly loadingAnalytics = signal(false);

    readonly marketLicenseStatusCounts = signal<MarketLicenseStatusCountResponse[]>([]);
    readonly marketProductsByRegion = signal<MarketProductCountByRegionResponse[]>([]);


    savingProduct = false;
    savingVariant = false;
    savingIngredient = false;
    savingVersion = false;
    changingVersionStatus = false;
    savingVersionIngredient = false;

    savingMarketProduct = false;
    savingMarketLicense = false;
    changingMarketLicenseStatus = false;
    readonly lifecycleVariantId = signal(0);
    readonly selectedMarketLicenseId = signal(0);
    readonly expiringUntilDate = signal('2026-12-31');

    readonly errorMessage = signal('');

    private readonly fb = inject(FormBuilder);

    readonly ingredientForm = this.fb.nonNullable.group({
        name: ['', [Validators.required, Validators.maxLength(255)]],
        chemicalFormula: ['', [Validators.maxLength(100)]],
        cas: ['', [Validators.required, Validators.maxLength(50)]],
        type: ['ACTIVE_SUBSTANCE' as const, Validators.required],
    });

    readonly productForm = this.fb.nonNullable.group({
        name: ['', [Validators.required, Validators.maxLength(255)]],
        description: ['', [Validators.maxLength(500)]],
        subcategoryId: [0, Validators.required],
        therapeuticAreaId: [0, Validators.required],
    });

    readonly variantForm = this.fb.nonNullable.group({
        productId: [0, Validators.required],
        form: ['', [Validators.required, Validators.maxLength(100)]],
        dosage: ['', [Validators.required, Validators.maxLength(100)]],
    });

    readonly versionForm = this.fb.nonNullable.group({
        variantId: [0, Validators.required],
        versionLabel: ['', [Validators.required, Validators.maxLength(50)]],
        description: ['', [Validators.maxLength(1000)]],
    });

    readonly versionIngredientForm = this.fb.nonNullable.group({
        variantVersionId: [0, Validators.required],
        ingredientId: [0, Validators.required],
        amount: [0, [Validators.required, Validators.min(0.0001)]],
        unit: ['', [Validators.required, Validators.maxLength(30)]],
    });


    readonly marketProductForm = this.fb.nonNullable.group({
      variantId: [0, Validators.required],
      regionId: [0, Validators.required],
      localName: ['', [Validators.required, Validators.maxLength(255)]],
      packagingDescription: ['', [Validators.maxLength(500)]],
      barcode: ['', [Validators.maxLength(100)]],
    });

    readonly marketLicenseForm = this.fb.nonNullable.group({
      marketProductId: [0, Validators.required],
      variantVersionId: [0, Validators.required],
      licenseNumber: ['', [Validators.required, Validators.maxLength(100)]],
      issuedAt: [''],
      validUntil: [''],
    });



  ngOnInit(): void {
    this.loadProducts();
    this.loadIngredients();
    this.loadVariants();
    this.loadVersions();
    this.loadVersionIngredients();
    this.loadReferenceData();
    this.loadMarketProducts();
    this.loadMarketLicenses();
    this.loadAnalytics();
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  setTab(tab: PortfolioTab): void {
    this.activeTab.set(tab);
  }

  loadProducts(): void {
  this.loadingProducts.set(true);
  this.clearError();

  this.portfolioService.getProducts().subscribe({
    next: (products) => {
      this.products.set(products);
      this.loadingProducts.set(false);
    },
    error: () => {
      this.loadingProducts.set(false);
      this.showError('Failed to load products.');
    },
  });
}

loadIngredients(): void {
  this.loadingIngredients.set(true);
  this.clearError();

  this.portfolioService.getIngredients().subscribe({
    next: (ingredients) => {
      this.ingredients.set(ingredients);
      this.loadingIngredients.set(false);
    },
    error: () => {
      this.loadingIngredients.set(false);
      this.showError('Failed to load ingredients.');
    },
  });
}

loadVariants(): void {
  this.loadingVariants.set(true);
  this.clearError();

  this.portfolioService.getVariants().subscribe({
    next: (variants) => {
      this.variants.set(variants);
      this.loadingVariants.set(false);
    },
    error: () => {
      this.loadingVariants.set(false);
      this.showError('Failed to load variants.');
    },
  });
}

loadVersions(): void {
  this.loadingVersions.set(true);
  this.clearError();

  this.portfolioService.getVariantVersions().subscribe({
    next: (versions) => {
      this.versions.set(versions);
      this.loadingVersions.set(false);
    },
    error: () => {
      this.loadingVersions.set(false);
      this.showError('Failed to load versions.');
    },
  });
}

loadVersionIngredients(): void {
  this.loadingVersionIngredients.set(true);
  this.clearError();

  this.portfolioService.getVersionIngredients().subscribe({
    next: (items) => {
      this.versionIngredients.set(items);
      this.loadingVersionIngredients.set(false);
    },
    error: () => {
      this.loadingVersionIngredients.set(false);
      this.showError('Failed to load version ingredients.');
    },
  });
}

createIngredient(): void {
  if (this.ingredientForm.invalid) {
    this.ingredientForm.markAllAsTouched();
    return;
  }

  this.savingIngredient = true;
  this.clearError();

  const payload = {
    name: this.ingredientForm.controls.name.value.trim(),
    chemicalFormula: this.ingredientForm.controls.chemicalFormula.value.trim(),
    cas: this.ingredientForm.controls.cas.value.trim(),
    type: this.ingredientForm.controls.type.value,
  };

  this.portfolioService.createIngredient(payload).subscribe({
    next: () => {
      this.savingIngredient = false;
      this.ingredientForm.reset({
        name: '',
        chemicalFormula: '',
        cas: '',
        type: 'ACTIVE_SUBSTANCE',
      });
      this.loadIngredients();
    },
    error: (error) => {
      this.savingIngredient = false;
      this.showError(error?.error?.message ?? 'Failed to create ingredient.');
    },
  });
}

loadReferenceData(): void {
  this.portfolioService.getSubcategories().subscribe({
    next: (items) => this.subcategories.set(items),
  });

  this.portfolioService.getTherapeuticAreas().subscribe({
    next: (items) => this.therapeuticAreas.set(items),
  });

  this.portfolioService.getRegions().subscribe({
    next: (items) => this.regions.set(items),
  });
}

createProduct(): void {
  if (this.productForm.invalid || this.productForm.controls.subcategoryId.value === 0 || this.productForm.controls.therapeuticAreaId.value === 0) {
    this.productForm.markAllAsTouched();
    return;
  }

  this.savingProduct = true;
  this.clearError();

  const payload = {
    name: this.productForm.controls.name.value.trim(),
    description: this.productForm.controls.description.value.trim(),
    subcategoryId: this.productForm.controls.subcategoryId.value,
    therapeuticAreaId: this.productForm.controls.therapeuticAreaId.value,
  };

  this.portfolioService.createProduct(payload).subscribe({
    next: () => {
      this.savingProduct = false;
      this.productForm.reset({
        name: '',
        description: '',
        subcategoryId: 0,
        therapeuticAreaId: 0,
      });
      this.loadProducts();
    },
    error: (error) => {
      this.savingProduct = false;
      this.showError(error?.error?.message ?? 'Failed to create product.');
    },
  });
}

createVariant(): void {
  if (this.variantForm.invalid || this.variantForm.controls.productId.value === 0) {
    this.variantForm.markAllAsTouched();
    return;
  }

  this.savingVariant = true;
  this.clearError();

  const payload = {
    productId: this.variantForm.controls.productId.value,
    form: this.variantForm.controls.form.value.trim(),
    dosage: this.variantForm.controls.dosage.value.trim(),
  };

  this.portfolioService.createVariant(payload).subscribe({
    next: () => {
      this.savingVariant = false;
      this.variantForm.reset({
        productId: 0,
        form: '',
        dosage: '',
      });
      this.loadVariants();
    },
    error: (error) => {
      this.savingVariant = false;
      this.showError(error?.error?.message ?? 'Failed to create variant.');
    },
  });
}

createVersion(): void {
  if (this.versionForm.invalid || this.versionForm.controls.variantId.value === 0) {
    this.versionForm.markAllAsTouched();
    return;
  }

  this.savingVersion = true;
  this.clearError();

  const payload = {
    variantId: this.versionForm.controls.variantId.value,
    versionLabel: this.versionForm.controls.versionLabel.value.trim(),
    description: this.versionForm.controls.description.value.trim(),
  };

  this.portfolioService.createVariantVersion(payload).subscribe({
    next: () => {
      this.savingVersion = false;
      this.versionForm.reset({
        variantId: 0,
        versionLabel: '',
        description: '',
      });
      this.loadVersions();
    },
    error: (error) => {
      this.savingVersion = false;
      this.showError(error?.error?.message ?? 'Failed to create version.');
    },
  });
}

activateVersion(versionId: number): void {
  this.changingVersionStatus = true;
  this.clearError();

  this.portfolioService.changeVariantVersionStatus(versionId, { status: 'ACTIVE' }).subscribe({
    next: () => {
      this.changingVersionStatus = false;
      this.loadVersions();
    },
    error: (error) => {
      this.changingVersionStatus = false;
      this.showError(error?.error?.message ?? 'Failed to activate version.');
    },
  });
}

createVersionIngredient(): void {
  if (
    this.versionIngredientForm.invalid ||
    this.versionIngredientForm.controls.variantVersionId.value === 0 ||
    this.versionIngredientForm.controls.ingredientId.value === 0
  ) {
    this.versionIngredientForm.markAllAsTouched();
    return;
  }

  this.savingVersionIngredient = true;
  this.clearError();

  const payload = {
    variantVersionId: this.versionIngredientForm.controls.variantVersionId.value,
    ingredientId: this.versionIngredientForm.controls.ingredientId.value,
    amount: this.versionIngredientForm.controls.amount.value,
    unit: this.versionIngredientForm.controls.unit.value.trim(),
  };

  this.portfolioService.createVersionIngredient(payload).subscribe({
    next: () => {
      this.savingVersionIngredient = false;
      this.versionIngredientForm.reset({
        variantVersionId: 0,
        ingredientId: 0,
        amount: 0,
        unit: '',
      });
      this.loadVersionIngredients();
    },
    error: (error) => {
      this.savingVersionIngredient = false;
      this.showError(error?.error?.message ?? 'Failed to add BOM item.');
    },
  });
}

//sprint2
loadMarketProducts(): void {
  this.loadingMarketProducts.set(true);
  this.clearError();

  this.portfolioService.getMarketProducts().subscribe({
    next: (items) => {
      this.marketProducts.set(items);
      this.loadingMarketProducts.set(false);
    },
    error: () => {
      this.loadingMarketProducts.set(false);
      this.showError('Failed to load market products.');
    },
  });
}

createMarketProduct(): void {
  if (
    this.marketProductForm.invalid ||
    this.marketProductForm.controls.variantId.value === 0 ||
    this.marketProductForm.controls.regionId.value === 0
  ) {
    this.marketProductForm.markAllAsTouched();
    return;
  }

  this.savingMarketProduct = true;
  this.clearError();

  const payload = {
    variantId: this.marketProductForm.controls.variantId.value,
    regionId: this.marketProductForm.controls.regionId.value,
    localName: this.marketProductForm.controls.localName.value.trim(),
    packagingDescription: this.marketProductForm.controls.packagingDescription.value.trim(),
    barcode: this.marketProductForm.controls.barcode.value.trim(),
  };

  this.portfolioService.createMarketProduct(payload).subscribe({
    next: () => {
      this.savingMarketProduct = false;
      this.marketProductForm.reset({
        variantId: 0,
        regionId: 0,
        localName: '',
        packagingDescription: '',
        barcode: '',
      });
      this.loadMarketProducts();
    },
    error: (error) => {
      this.savingMarketProduct = false;
      this.showError(error?.error?.message ?? 'Failed to create market product.');
    },
  });
}

loadMarketLicenses(): void {
  this.loadingMarketLicenses.set(true);
  this.clearError();

  this.portfolioService.getMarketLicenses().subscribe({
    next: (items) => {
      this.marketLicenses.set(items);
      this.loadingMarketLicenses.set(false);
    },
    error: () => {
      this.loadingMarketLicenses.set(false);
      this.showError('Failed to load market licenses.');
    },
  });
}

createMarketLicense(): void {
  if (
    this.marketLicenseForm.invalid ||
    this.marketLicenseForm.controls.marketProductId.value === 0 ||
    this.marketLicenseForm.controls.variantVersionId.value === 0
  ) {
    this.marketLicenseForm.markAllAsTouched();
    return;
  }

  this.savingMarketLicense = true;
  this.clearError();

  const payload = {
    marketProductId: this.marketLicenseForm.controls.marketProductId.value,
    variantVersionId: this.marketLicenseForm.controls.variantVersionId.value,
    licenseNumber: this.marketLicenseForm.controls.licenseNumber.value.trim(),
    issuedAt: this.marketLicenseForm.controls.issuedAt.value || null,
    validUntil: this.marketLicenseForm.controls.validUntil.value || null,
  };

  this.portfolioService.createMarketLicense(payload).subscribe({
    next: () => {
      this.savingMarketLicense = false;
      this.marketLicenseForm.reset({
        marketProductId: 0,
        variantVersionId: 0,
        licenseNumber: '',
        issuedAt: '',
        validUntil: '',
      });
      this.loadMarketLicenses();
    },
    error: (error) => {
      this.savingMarketLicense = false;
      this.showError(error?.error?.message ?? 'Failed to create market license.');
    },
  });
}

changeMarketLicenseStatus(id: number, status: MarketLicenseStatus): void {
  this.changingMarketLicenseStatus = true;
  this.clearError();

  this.portfolioService.changeMarketLicenseStatus(id, { status }).subscribe({
    next: () => {
      this.changingMarketLicenseStatus = false;
      this.loadMarketLicenses();
      if (this.selectedMarketLicenseId() === id) {
        this.loadMarketLicenseHistory(id);
      }
    },
    error: (error) => {
      this.changingMarketLicenseStatus = false;
      this.showError(error?.error?.message ?? 'Failed to change market license status.');
    },
  });
}

loadMarketLicenseHistory(id: number): void {
  this.selectedMarketLicenseId.set(id);
  this.loadingMarketLicenseHistory.set(true);
  this.clearError();

  this.portfolioService.getMarketLicenseHistory(id).subscribe({
    next: (items) => {
      this.marketLicenseHistory.set(items);
      this.loadingMarketLicenseHistory.set(false);
    },
    error: () => {
      this.loadingMarketLicenseHistory.set(false);
      this.showError('Failed to load market license history.');
    },
  });
}

loadVariantLifecycleHistory(variantId: number): void {
  if (!variantId) {
    this.variantLifecycleHistory.set([]);
    return;
  }

  this.lifecycleVariantId.set(variantId);
  this.loadingVariantLifecycleHistory.set(true);
  this.clearError();

  this.portfolioService.getVariantLifecycleHistory(variantId).subscribe({
    next: (items) => {
      this.variantLifecycleHistory.set(items);
      this.loadingVariantLifecycleHistory.set(false);
    },
    error: () => {
      this.loadingVariantLifecycleHistory.set(false);
      this.showError('Failed to load variant lifecycle history.');
    },
  });
}

loadAnalytics(): void {
  this.loadingAnalytics.set(true);
  this.clearError();

  this.portfolioService.getVariantVersionStatusCount().subscribe({
    next: (items) => this.versionStatusCounts.set(items),
    error: () => this.showError('Failed to load status analytics.'),
  });

  this.portfolioService.getProductsByTherapeuticArea().subscribe({
    next: (items) => this.productsByTherapeuticArea.set(items),
    error: () => this.showError('Failed to load therapeutic area analytics.'),
  });

  this.portfolioService.getMarketLicenseStatusCount().subscribe({
    next: (items) => this.marketLicenseStatusCounts.set(items),
    error: () => this.showError('Failed to load market license status analytics.'),
  });

  this.portfolioService.getMarketProductsByRegion().subscribe({
    next: (items) => this.marketProductsByRegion.set(items),
    error: () => this.showError('Failed to load market products by region analytics.'),
  });

  this.portfolioService.getLicensesExpiringUntil(this.expiringUntilDate()).subscribe({
    next: (items) => {
      this.expiringLicenses.set(items);
      this.loadingAnalytics.set(false);
    },
    error: () => {
      this.loadingAnalytics.set(false);
      this.showError('Failed to load expiring licenses.');
    },
  });
}

downloadAnalyticsReport(): void {
  this.portfolioService
    .downloadPortfolioAnalyticsReport(this.expiringUntilDate())
    .subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);

        const link = document.createElement('a');
        link.href = url;
        link.download = 'portfolio-analytics-report.pdf';

        link.click();

        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.showError('Failed to generate analytics report.');
      },
    });
}

loadVersionHistory(versionId: number): void {
  this.selectedVersionId.set(versionId);
  this.loadingVersionHistory.set(true);
  this.clearError();

  this.portfolioService.getVariantVersionHistory(versionId).subscribe({
    next: (items) => {
      this.versionHistory.set(items);
      this.loadingVersionHistory.set(false);
    },
    error: () => {
      this.loadingVersionHistory.set(false);
      this.showError('Failed to load version history.');
    },
  });
}

private showError(message: string): void {
  this.transientMessages.set(
    this,
    'errorMessage',
    (value) => this.errorMessage.set(value),
    () => this.errorMessage(),
    message,
    ERROR_MESSAGE_MS
  );
}

private clearError(): void {
  this.transientMessages.clear(
    this,
    'errorMessage',
    () => this.errorMessage.set('')
  );
}
}
