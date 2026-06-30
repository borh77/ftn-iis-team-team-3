import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ReplacementSuggestion, ValidationResult } from '../../core/procurement.models';

@Component({
  selector: 'app-validation-result',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './validation-result.component.html',
  styleUrl: './validation-result.component.css',
})
export class ValidationResultComponent {
  @Input({ required: true }) result!: ValidationResult;
  @Input() confirming = false;
  @Input() confirmed = false;
  @Input() acceptedReplacementKeys = new Set<string>();
  @Output() replacementAccepted = new EventEmitter<ReplacementSuggestion>();
  @Output() confirmProcurement = new EventEmitter<void>();

  acceptReplacement(replacement: ReplacementSuggestion): void {
    this.replacementAccepted.emit(replacement);
  }

  isReplacementAccepted(replacement: ReplacementSuggestion): boolean {
    return this.acceptedReplacementKeys.has(this.replacementKey(replacement));
  }

  get acceptedReplacements(): ReplacementSuggestion[] {
    return this.result.replacements.filter((replacement) => this.isReplacementAccepted(replacement));
  }

  get hasPendingReplacements(): boolean {
    return this.result.replacements.some((replacement) => !this.isReplacementAccepted(replacement));
  }

  get canShowConfirmStep(): boolean {
    return this.result.invalidItems.length === 0;
  }

  get finalItemCount(): number {
    return this.result.validatedItems.length + this.acceptedReplacements.length;
  }

  get displayTotalPrice(): number {
    const validatedTotal = this.result.validatedItems.reduce((sum, item) => sum + item.lineTotal, 0);
    const acceptedReplacementTotal = this.acceptedReplacements.reduce((sum, replacement) => sum + replacement.lineTotal, 0);
    return validatedTotal + acceptedReplacementTotal;
  }

  get confirmDisabledReason(): string {
    if (this.confirmed) {
      return 'This procurement order has already been submitted.';
    }
    if (this.result.invalidItems.length > 0) {
      return 'Fix or remove problematic items before confirming procurement.';
    }
    if (this.hasPendingReplacements) {
      return 'Accept all replacement suggestions before confirming procurement.';
    }
    if (this.finalItemCount === 0) {
      return 'There are no valid items to confirm.';
    }
    return '';
  }

  get canConfirm(): boolean {
    return !this.confirming && !this.confirmed && !this.confirmDisabledReason;
  }

  confirm(): void {
    if (!this.canConfirm) {
      return;
    }
    this.confirmProcurement.emit();
  }

  replacementKey(replacement: ReplacementSuggestion): string {
    return `${replacement.oldVariantId}:${replacement.newVariantId}:${replacement.requestedQuantity}`;
  }
}
