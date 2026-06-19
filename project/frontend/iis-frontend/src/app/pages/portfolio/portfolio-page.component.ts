import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { PortfolioService } from '../../core/portfolio/portfolio.service';
import {
  IngredientResponse,
  ProductResponse,
  VariantResponse,
  VariantVersionResponse,
  VariantVersionIngredientsResponse,
  SubcategoryResponse,
  TherapeuticAreaResponse,
} from '../../core/portfolio/portfolio.models';

import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

type PortfolioTab =
  | 'products'
  | 'variants'
  | 'versions'
  | 'ingredients'
  | 'bom';

@Component({
  selector: 'app-portfolio-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './portfolio-page.component.html',
  styleUrl: './portfolio-page.component.css',
})
export class PortfolioPageComponent implements OnInit {
  private readonly portfolioService = inject(PortfolioService);

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

    savingProduct = false;
    savingVariant = false;
    savingIngredient = false;
    savingVersion = false;
    changingVersionStatus = false;
    savingVersionIngredient = false;

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


  ngOnInit(): void {
    this.loadProducts();
    this.loadIngredients();
    this.loadVariants();
    this.loadVersions();
    this.loadVersionIngredients();
    this.loadReferenceData();
  }

  setTab(tab: PortfolioTab): void {
    this.activeTab.set(tab);
  }

  loadProducts(): void {
  this.loadingProducts.set(true);
  this.errorMessage.set('');

  this.portfolioService.getProducts().subscribe({
    next: (products) => {
      this.products.set(products);
      this.loadingProducts.set(false);
    },
    error: () => {
      this.loadingProducts.set(false);
      this.errorMessage.set('Failed to load products.');
    },
  });
}

loadIngredients(): void {
  this.loadingIngredients.set(true);
  this.errorMessage.set('');

  this.portfolioService.getIngredients().subscribe({
    next: (ingredients) => {
      this.ingredients.set(ingredients);
      this.loadingIngredients.set(false);
    },
    error: () => {
      this.loadingIngredients.set(false);
      this.errorMessage.set('Failed to load ingredients.');
    },
  });
}

loadVariants(): void {
  this.loadingVariants.set(true);
  this.errorMessage.set('');

  this.portfolioService.getVariants().subscribe({
    next: (variants) => {
      this.variants.set(variants);
      this.loadingVariants.set(false);
    },
    error: () => {
      this.loadingVariants.set(false);
      this.errorMessage.set('Failed to load variants.');
    },
  });
}

loadVersions(): void {
  this.loadingVersions.set(true);
  this.errorMessage.set('');

  this.portfolioService.getVariantVersions().subscribe({
    next: (versions) => {
      this.versions.set(versions);
      this.loadingVersions.set(false);
    },
    error: () => {
      this.loadingVersions.set(false);
      this.errorMessage.set('Failed to load versions.');
    },
  });
}

loadVersionIngredients(): void {
  this.loadingVersionIngredients.set(true);
  this.errorMessage.set('');

  this.portfolioService.getVersionIngredients().subscribe({
    next: (items) => {
      this.versionIngredients.set(items);
      this.loadingVersionIngredients.set(false);
    },
    error: () => {
      this.loadingVersionIngredients.set(false);
      this.errorMessage.set('Failed to load version ingredients.');
    },
  });
}

createIngredient(): void {
  if (this.ingredientForm.invalid) {
    this.ingredientForm.markAllAsTouched();
    return;
  }

  this.savingIngredient = true;
  this.errorMessage.set('');

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
      this.errorMessage.set(error?.error?.message ?? 'Failed to create ingredient.');
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
}

createProduct(): void {
  if (this.productForm.invalid || this.productForm.controls.subcategoryId.value === 0 || this.productForm.controls.therapeuticAreaId.value === 0) {
    this.productForm.markAllAsTouched();
    return;
  }

  this.savingProduct = true;
  this.errorMessage.set('');

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
      this.errorMessage.set(error?.error?.message ?? 'Failed to create product.');
    },
  });
}

createVariant(): void {
  if (this.variantForm.invalid || this.variantForm.controls.productId.value === 0) {
    this.variantForm.markAllAsTouched();
    return;
  }

  this.savingVariant = true;
  this.errorMessage.set('');

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
      this.errorMessage.set(error?.error?.message ?? 'Failed to create variant.');
    },
  });
}

createVersion(): void {
  if (this.versionForm.invalid || this.versionForm.controls.variantId.value === 0) {
    this.versionForm.markAllAsTouched();
    return;
  }

  this.savingVersion = true;
  this.errorMessage.set('');

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
      this.errorMessage.set(error?.error?.message ?? 'Failed to create version.');
    },
  });
}

activateVersion(versionId: number): void {
  this.changingVersionStatus = true;
  this.errorMessage.set('');

  this.portfolioService.changeVariantVersionStatus(versionId, { status: 'ACTIVE' }).subscribe({
    next: () => {
      this.changingVersionStatus = false;
      this.loadVersions();
    },
    error: (error) => {
      this.changingVersionStatus = false;
      this.errorMessage.set(error?.error?.message ?? 'Failed to activate version.');
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
  this.errorMessage.set('');

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
      this.errorMessage.set(error?.error?.message ?? 'Failed to add BOM item.');
    },
  });
}

}