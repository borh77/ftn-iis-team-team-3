import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ReactiveFormsModule, UntypedFormArray, UntypedFormGroup } from '@angular/forms';

@Component({
  selector: 'app-pricelist-wizard-thresholds-step',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="step-head">
      <div>
        <h3>Quantity thresholds</h3>
        <p class="muted">Set min/max quantities and leave the final max empty for an open-ended range.</p>
      </div>
    </div>

    @if (!items.length) {
      <p class="inline-error">Add at least one item before configuring thresholds.</p>
    }

    <form class="items-list" [formGroup]="form">
      <div formArrayName="items">
        @for (item of items.controls; track $index; let itemIndex = $index) {
          <section class="item-shell" [formGroupName]="itemIndex">
            <header class="item-head">
              <div>
                <p class="kicker">Variant ID: {{ itemGroup(itemIndex).controls['variantId'].value }}</p>
                <h4>{{ itemGroup(itemIndex).controls['variantName'].value }}</h4>
              </div>
              <button type="button" class="secondary" (click)="addThreshold.emit(itemIndex)">
                Add threshold
              </button>
            </header>

            <div class="threshold-stack" formArrayName="thresholds">
              @for (threshold of thresholdsFor(itemIndex).controls; track $index; let thresholdIndex = $index) {
                <div class="threshold-grid" [formGroupName]="thresholdIndex">
                  <label>
                    <span>Min quantity</span>
                    <input type="number" min="1" formControlName="quantityFrom" />
                    @if (thresholdError(itemIndex, thresholdIndex, 'quantityFrom')) {
                      <small class="field-error">{{ thresholdError(itemIndex, thresholdIndex, 'quantityFrom') }}</small>
                    }
                  </label>

                  <label>
                    <span>Max quantity</span>
                    <input type="number" min="1" formControlName="quantityTo" placeholder="Open ended" />
                    @if (thresholdError(itemIndex, thresholdIndex, 'quantityTo')) {
                      <small class="field-error">{{ thresholdError(itemIndex, thresholdIndex, 'quantityTo') }}</small>
                    }
                  </label>

                  <label>
                    <span>Price</span>
                    <input type="number" min="0.01" step="0.01" formControlName="price" />
                    @if (thresholdError(itemIndex, thresholdIndex, 'price')) {
                      <small class="field-error">{{ thresholdError(itemIndex, thresholdIndex, 'price') }}</small>
                    }
                  </label>

                  <button type="button" class="ghost" (click)="removeThreshold.emit({ itemIndex, thresholdIndex })">
                    Remove
                  </button>
                </div>
              }
            </div>
          </section>
        }
      </div>
    </form>
  `,
})
export class PricelistWizardThresholdsStepComponent {
  @Input({ required: true }) form!: UntypedFormGroup;
  @Output() addThreshold = new EventEmitter<number>();
  @Output() removeThreshold = new EventEmitter<{ itemIndex: number; thresholdIndex: number }>();

  get items(): UntypedFormArray {
    return this.form.get('items') as UntypedFormArray;
  }

  itemGroup(index: number): UntypedFormGroup {
    return this.items.at(index) as UntypedFormGroup;
  }

  thresholdsFor(itemIndex: number): UntypedFormArray {
    return this.itemGroup(itemIndex).get('thresholds') as UntypedFormArray;
  }

  thresholdError(itemIndex: number, thresholdIndex: number, controlName: string): string {
    const control = (this.thresholdsFor(itemIndex).at(thresholdIndex) as UntypedFormGroup).get(controlName);
    if (!control || (!control.touched && !control.dirty)) {
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
}
