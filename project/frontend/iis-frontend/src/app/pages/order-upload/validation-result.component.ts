import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ValidationResult } from '../../core/procurement.models';

@Component({
  selector: 'app-validation-result',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './validation-result.component.html',
  styleUrl: './validation-result.component.css',
})
export class ValidationResultComponent {
  @Input({ required: true }) result!: ValidationResult;
}
