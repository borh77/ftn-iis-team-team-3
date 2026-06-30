import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ReactiveFormsModule, UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { Category, Product, Subcategory, Variant } from '../../core/portfolio.models';

@Component({
  selector: 'app-pricelist-wizard-items-step',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="step-head">
      <div>
        <h3>Pricelist items</h3>
        <p class="muted">Add medicine variants before configuring quantity thresholds.</p>
      </div>
      <button type="button" class="secondary" (click)="addItem.emit()">Add item</button>
    </div>

    <form class="items-list" [formGroup]="form">
      <div formArrayName="items">
        @for (item of items.controls; track $index; let itemIndex = $index) {
          <section class="item-shell" [formGroupName]="itemIndex">
            <header class="item-head">
              <div>
                <p class="kicker">Item {{ itemIndex + 1 }}</p>
                <h4>Medicine variant</h4>
              </div>
              <button type="button" class="ghost" (click)="removeItem.emit(itemIndex)">Remove</button>
            </header>

            <div class="form-grid">
              <label>
                <span>Category</span>
                <select formControlName="categoryId" (change)="categoryChanged.emit(itemIndex)">
                  <option [ngValue]="null">Select category</option>
                  @for (category of categories; track category.id) {
                    <option [ngValue]="category.id">{{ category.name }}</option>
                  }
                </select>
                @if (controlError(itemIndex, 'categoryId')) {
                  <small class="field-error">{{ controlError(itemIndex, 'categoryId') }}</small>
                }
              </label>

              <label>
                <span>Subcategory</span>
                <select formControlName="subcategoryId" (change)="subcategoryChanged.emit(itemIndex)">
                  <option [ngValue]="null">Select subcategory</option>
                  @for (subcategory of (subcategoriesByItem[itemIndex] ?? []); track subcategory.id) {
                    <option [ngValue]="subcategory.id">{{ subcategory.name }}</option>
                  }
                </select>
                @if (controlError(itemIndex, 'subcategoryId')) {
                  <small class="field-error">{{ controlError(itemIndex, 'subcategoryId') }}</small>
                }
              </label>

              <label>
                <span>Product</span>
                <select formControlName="productId" (change)="productChanged.emit(itemIndex)">
                  <option [ngValue]="null">Select product</option>
                  @for (product of (productsByItem[itemIndex] ?? []); track product.id) {
                    <option [ngValue]="product.id">{{ product.name }}</option>
                  }
                </select>
                @if (controlError(itemIndex, 'productId')) {
                  <small class="field-error">{{ controlError(itemIndex, 'productId') }}</small>
                }
              </label>

              <label>
                <span>Variant</span>
                <select formControlName="variantId">
                  <option [ngValue]="null">Select variant</option>
                  @if (itemGroup(itemIndex).controls['existingVariantName'].value) {
                    <option [ngValue]="itemGroup(itemIndex).controls['variantId'].value">
                      {{ itemGroup(itemIndex).controls['existingVariantName'].value }}
                    </option>
                  }
                  @for (variant of variantsForItem(itemIndex); track variant.id) {
                    <option
                      [ngValue]="variant.id"
                      [disabled]="isVariantSelectedElsewhere(variant.id, itemIndex)"
                    >
                      {{ variant.productName }} - {{ variant.form }} / {{ variant.dosage }}
                    </option>
                  }
                </select>
                @if (controlError(itemIndex, 'variantId')) {
                  <small class="field-error">{{ controlError(itemIndex, 'variantId') }}</small>
                }
              </label>
            </div>
          </section>
        }
      </div>
    </form>

    @if (duplicateVariantMessage) {
      <p class="inline-error">{{ duplicateVariantMessage }}</p>
    }
  `,
})
export class PricelistWizardItemsStepComponent {
  @Input({ required: true }) form!: UntypedFormGroup;
  @Input() categories: Category[] = [];
  @Input() availableVariants: Variant[] = [];
  @Input() subcategoriesByItem: Record<number, Subcategory[] | undefined> = {};
  @Input() productsByItem: Record<number, Product[] | undefined> = {};
  @Input() variantsByItem: Record<number, Variant[] | undefined> = {};
  @Input() duplicateVariantMessage = '';
  @Output() addItem = new EventEmitter<void>();
  @Output() removeItem = new EventEmitter<number>();
  @Output() categoryChanged = new EventEmitter<number>();
  @Output() subcategoryChanged = new EventEmitter<number>();
  @Output() productChanged = new EventEmitter<number>();

  get items(): UntypedFormArray {
    return this.form.get('items') as UntypedFormArray;
  }

  itemGroup(index: number): UntypedFormGroup {
    return this.items.at(index) as UntypedFormGroup;
  }

  variantsForItem(index: number): Variant[] {
    const productId = this.itemGroup(index).controls['productId'].value;
    if (productId) {
      return this.variantsByItem[index] ?? [];
    }
    return this.availableVariants;
  }

  isVariantSelectedElsewhere(variantId: number, currentIndex: number): boolean {
    return this.items.controls.some((control, index) => {
      return index !== currentIndex && Number(control.get('variantId')?.value) === variantId;
    });
  }

  controlError(itemIndex: number, controlName: string): string {
    const control = this.itemGroup(itemIndex).get(controlName);
    if (!control || (!control.touched && !control.dirty)) {
      return '';
    }
    if (control.hasError('required')) {
      return 'This field is required.';
    }
    return '';
  }
}
