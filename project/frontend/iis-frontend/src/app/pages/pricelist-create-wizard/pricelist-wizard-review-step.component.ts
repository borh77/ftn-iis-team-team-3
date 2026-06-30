import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { PricelistWizardSummary } from '../../core/pricelist.models';

@Component({
  selector: 'app-pricelist-wizard-review-step',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (loading) {
      <p class="muted">Loading summary...</p>
    } @else if (!summary?.pricelist) {
      <p class="inline-error">Summary could not be loaded.</p>
    } @else {
      <section class="review-grid">
        <div>
          <p class="kicker">Basic info</p>
          <dl class="summary-list">
            <div>
              <dt>Region</dt>
              <dd>{{ summary?.pricelist?.regionName || '-' }}</dd>
            </div>
            <div>
              <dt>Customer segment</dt>
              <dd>{{ summary?.pricelist?.customerSegment || '-' }}</dd>
            </div>
            <div>
              <dt>Currency</dt>
              <dd>{{ summary?.pricelist?.currency || '-' }}</dd>
            </div>
            <div>
              <dt>Period</dt>
              <dd>
                {{ summary?.pricelist?.periodStart ? (summary?.pricelist?.periodStart | date:'short') : '-' }}
                -
                {{ summary?.pricelist?.periodEnd ? (summary?.pricelist?.periodEnd | date:'short') : '-' }}
              </dd>
            </div>
          </dl>
        </div>

        <div>
          <p class="kicker">Items</p>
          @if (summary?.pricelist?.items?.length) {
            <div class="review-items">
              @for (item of summary?.pricelist?.items; track item.variantId) {
                <article class="review-item">
                  <strong>{{ item.variantName }}</strong>
                  <span class="muted">Variant ID: {{ item.variantId }}</span>
                  <ul>
                    @for (threshold of item.thresholds; track threshold.quantityFrom + '-' + (threshold.quantityTo ?? 'open')) {
                      <li>
                        {{ threshold.quantityFrom }} - {{ threshold.quantityTo ?? 'open' }}
                        | {{ threshold.price | number:'1.2-2' }} {{ summary?.pricelist?.currency || '' }}
                      </li>
                    }
                  </ul>
                </article>
              }
            </div>
          } @else {
            <p class="muted">No items added.</p>
          }
        </div>
      </section>

      @if (summary?.validationMessages?.length) {
        <div class="inline-error">
          @for (message of summary?.validationMessages; track message) {
            <p>{{ message }}</p>
          }
        </div>
      } @else {
        <p class="success-box">Pricelist is ready to submit for review.</p>
      }
    }
  `,
})
export class PricelistWizardReviewStepComponent {
  @Input() summary: PricelistWizardSummary | null = null;
  @Input() loading = false;
}
