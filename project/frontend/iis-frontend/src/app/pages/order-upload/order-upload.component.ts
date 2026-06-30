import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, ElementRef, OnDestroy, ViewChild, inject } from '@angular/core';
import { Router } from '@angular/router';
import { finalize, take } from 'rxjs';
import { ProcurementService } from '../../core/procurement.service';
import { ConfirmProcurementItem, ProcurementOrder, ReplacementSuggestion, ValidationResult } from '../../core/procurement.models';
import { ERROR_MESSAGE_MS, SUCCESS_MESSAGE_MS, TransientMessageService } from '../../core/transient-message.service';
import { ValidationResultComponent } from './validation-result.component';

@Component({
  selector: 'app-order-upload',
  standalone: true,
  imports: [CommonModule, ValidationResultComponent],
  templateUrl: './order-upload.component.html',
  styleUrl: './order-upload.component.css',
})
export class OrderUploadComponent implements OnDestroy {
  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

  private readonly procurementService = inject(ProcurementService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly transientMessages = inject(TransientMessageService);
  private readonly router = inject(Router);

  selectedFile: File | null = null;
  result: ValidationResult | null = null;
  loading = false;
  confirmLoading = false;
  errorMessage = '';
  confirmSuccessMessage = '';
  confirmedOrder: ProcurementOrder | null = null;
  acceptedReplacementKeys = new Set<string>();
  dragActive = false;

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  onFileInputChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.setSelectedFile(file);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.dragActive = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.dragActive = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragActive = false;
    const file = event.dataTransfer?.files?.[0] ?? null;
    this.setSelectedFile(file);
  }

  openFilePicker(): void {
    this.fileInput?.nativeElement.click();
  }

  validateSelectedFile(): void {
    if (!this.selectedFile) {
      this.showError('Please choose a CSV document first.');
      return;
    }

    if (!this.isSupportedFile(this.selectedFile)) {
      this.showError('Only CSV files are supported for procurement validation.');
      return;
    }

    this.loading = true;
    this.clearMessages();
    this.result = null;
    this.confirmedOrder = null;
    this.acceptedReplacementKeys.clear();

    this.procurementService.validateOrderDocument(this.selectedFile)
      .pipe(
        take(1),
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (result) => {
          this.result = result;
          this.clearError();
          this.cdr.detectChanges();
        },
        error: (error: HttpErrorResponse) => {
          this.result = null;
          this.showError(this.createErrorMessage(error));
          this.cdr.detectChanges();
        },
      });
  }

  clearSelection(): void {
    this.selectedFile = null;
    this.result = null;
    this.confirmedOrder = null;
    this.acceptedReplacementKeys.clear();
    this.clearMessages();
    if (this.fileInput?.nativeElement) {
      this.fileInput.nativeElement.value = '';
    }
  }

  acceptReplacement(replacement: ReplacementSuggestion): void {
    this.acceptedReplacementKeys = new Set([
      ...this.acceptedReplacementKeys,
      this.replacementKey(replacement),
    ]);
  }

  confirmProcurement(): void {
    const items = this.confirmationItems();
    if (!items.length) {
      this.showError('Procurement cannot be confirmed because it contains invalid items.');
      return;
    }

    this.confirmLoading = true;
    this.clearMessages();
    this.procurementService.confirmProcurement({
      sourceFileName: this.selectedFile?.name ?? null,
      items,
    })
      .pipe(
        take(1),
        finalize(() => {
          this.confirmLoading = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (order) => {
          this.confirmedOrder = order;
          this.showSuccess(`Procurement order #${order.id} was submitted.`);
          this.cdr.detectChanges();
        },
        error: (error: HttpErrorResponse) => {
          this.showError(this.createErrorMessage(error));
          this.cdr.detectChanges();
        },
      });
  }

  viewMyProcurements(): void {
    this.router.navigate(['/procurements']);
  }

  private setSelectedFile(file: File | null): void {
    this.selectedFile = file;
    this.result = null;
    this.confirmedOrder = null;
    this.acceptedReplacementKeys.clear();
    this.clearMessages();

    if (file && !this.isSupportedFile(file)) {
      this.showError('Only CSV files are supported for procurement validation.');
    }
  }

  private isSupportedFile(file: File): boolean {
    const name = file.name.toLowerCase();
    return name.endsWith('.csv');
  }

  private createErrorMessage(error: HttpErrorResponse): string {
    if (typeof error.error?.error === 'string' && error.error.error.trim()) {
      return error.error.error.trim();
    }

    if (typeof error.error?.message === 'string' && error.error.message.trim()) {
      return error.error.message.trim();
    }

    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error.trim();
    }

    if (error.status === 403) {
      return 'You are not allowed to validate procurement documents.';
    }

    return 'The document could not be validated.';
  }

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private showSuccess(message: string): void {
    this.transientMessages.setField(this, 'confirmSuccessMessage', message, SUCCESS_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private clearError(): void {
    this.transientMessages.clearField(this, 'errorMessage', () => this.cdr.detectChanges());
  }

  private clearSuccess(): void {
    this.transientMessages.clearField(this, 'confirmSuccessMessage', () => this.cdr.detectChanges());
  }

  private clearMessages(): void {
    this.clearError();
    this.clearSuccess();
  }

  private confirmationItems(): ConfirmProcurementItem[] {
    if (!this.result || this.result.invalidItems.length > 0) {
      return [];
    }

    const validatedItems = this.result.validatedItems.map((item) => ({
      variantId: item.variantId,
      requestedQuantity: item.requestedQuantity,
    }));
    const replacementItems = this.result.replacements
      .filter((replacement) => this.acceptedReplacementKeys.has(this.replacementKey(replacement)))
      .map((replacement) => ({
        variantId: replacement.newVariantId,
        requestedQuantity: replacement.requestedQuantity,
        originalVariantId: replacement.oldVariantId,
        originalVariantName: replacement.oldVariantName,
        replacementAccepted: true,
      }));

    return [...validatedItems, ...replacementItems];
  }

  private replacementKey(replacement: ReplacementSuggestion): string {
    return `${replacement.oldVariantId}:${replacement.newVariantId}:${replacement.requestedQuantity}`;
  }
}
