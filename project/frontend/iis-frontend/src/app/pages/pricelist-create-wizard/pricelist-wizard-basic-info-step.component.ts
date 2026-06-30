import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ReactiveFormsModule, UntypedFormGroup } from '@angular/forms';
import { Region } from '../../core/region.model';

@Component({
  selector: 'app-pricelist-wizard-basic-info-step',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <form class="wizard-form" [formGroup]="form">
      <div class="form-grid">
        <label>
          <span>Region</span>
          <select formControlName="regionId">
            <option [ngValue]="null">Select region</option>
            @for (region of regions; track region.id) {
              <option [ngValue]="region.id">{{ region.name }} ({{ region.code }})</option>
            }
          </select>
          @if (fieldError('regionId')) {
            <small class="field-error">{{ fieldError('regionId') }}</small>
          }
        </label>

        <label>
          <span>Customer segment</span>
          <input type="text" formControlName="customerSegment" placeholder="Pharmacy chains" />
          @if (fieldError('customerSegment')) {
            <small class="field-error">{{ fieldError('customerSegment') }}</small>
          }
        </label>

        <label>
          <span>Currency</span>
          <input type="text" formControlName="currency" placeholder="RSD" maxlength="3" />
          @if (fieldError('currency')) {
            <small class="field-error">{{ fieldError('currency') }}</small>
          }
        </label>

        <label>
          <span>Period start</span>
          <input type="datetime-local" formControlName="periodStart" [min]="minPeriodStart" />
          @if (fieldError('periodStart')) {
            <small class="field-error">{{ fieldError('periodStart') }}</small>
          }
        </label>

        <label>
          <span>Period end</span>
          <input type="datetime-local" formControlName="periodEnd" />
          @if (fieldError('periodEnd')) {
            <small class="field-error">{{ fieldError('periodEnd') }}</small>
          }
        </label>
      </div>

      @if (form.touched && form.hasError('periodOrder')) {
        <p class="inline-error">Period start must be before period end.</p>
      }
    </form>
  `,
})
export class PricelistWizardBasicInfoStepComponent {
  @Input({ required: true }) form!: UntypedFormGroup;
  @Input() regions: Region[] = [];
  @Input() minPeriodStart = '';

  fieldError(controlName: string): string {
    const control = this.form.get(controlName);
    if (controlName === 'periodStart' && this.form.hasError('periodStartInPast')) {
      return 'Pricelist start date cannot be in the past.';
    }
    if (!control || (!control.touched && !control.dirty)) {
      return '';
    }
    if (control.hasError('required')) {
      return 'This field is required.';
    }
    if (control.hasError('maxlength')) {
      return 'Value is too long.';
    }
    if (control.hasError('pattern')) {
      return 'Currency must be a three-letter ISO code.';
    }
    return '';
  }
}
