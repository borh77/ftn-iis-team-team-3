import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
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

  private readonly acceptedReplacementsSet = new Set<ReplacementSuggestion>();

  acceptReplacement(replacement: ReplacementSuggestion): void {
    this.acceptedReplacementsSet.add(replacement);
  }

  isReplacementAccepted(replacement: ReplacementSuggestion): boolean {
    return this.acceptedReplacementsSet.has(replacement);
  }

  get acceptedReplacements(): ReplacementSuggestion[] {
    return this.result.replacements.filter((replacement) => this.isReplacementAccepted(replacement));
  }

  get hasPendingReplacements(): boolean {
    return this.result.replacements.some((replacement) => !this.isReplacementAccepted(replacement));
  }

  get canShowConfirmStep(): boolean {
    return this.result.invalidItems.length === 0 && !this.hasPendingReplacements;
  }

}
