import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, ElementRef, ViewChild, inject } from '@angular/core';
import { finalize, take } from 'rxjs';
import { ProcurementService } from '../../core/procurement.service';
import { ValidationResult } from '../../core/procurement.models';
import { ValidationResultComponent } from './validation-result.component';

@Component({
  selector: 'app-order-upload',
  standalone: true,
  imports: [CommonModule, ValidationResultComponent],
  templateUrl: './order-upload.component.html',
  styleUrl: './order-upload.component.css',
})
export class OrderUploadComponent {
  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

  private readonly procurementService = inject(ProcurementService);
  private readonly cdr = inject(ChangeDetectorRef);

  selectedFile: File | null = null;
  result: ValidationResult | null = null;
  loading = false;
  errorMessage = '';
  dragActive = false;

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
      this.errorMessage = 'Please choose a JSON or CSV document first.';
      return;
    }

    if (!this.isSupportedFile(this.selectedFile)) {
      this.errorMessage = 'Unsupported file type. Please upload a JSON or CSV document.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.result = null;

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
          this.errorMessage = '';
          this.cdr.detectChanges();
        },
        error: (error: HttpErrorResponse) => {
          this.result = null;
          this.errorMessage = this.createErrorMessage(error);
          this.cdr.detectChanges();
        },
      });
  }

  clearSelection(): void {
    this.selectedFile = null;
    this.result = null;
    this.errorMessage = '';
    if (this.fileInput?.nativeElement) {
      this.fileInput.nativeElement.value = '';
    }
  }

  private setSelectedFile(file: File | null): void {
    this.selectedFile = file;
    this.result = null;
    this.errorMessage = '';

    if (file && !this.isSupportedFile(file)) {
      this.errorMessage = 'Unsupported file type. Please upload a JSON or CSV document.';
    }
  }

  private isSupportedFile(file: File): boolean {
    const name = file.name.toLowerCase();
    const type = file.type.toLowerCase();
    return name.endsWith('.json')
      || name.endsWith('.csv')
      || type === 'application/json'
      || type === 'text/csv'
      || type === 'application/csv';
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
}
