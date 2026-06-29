import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Region } from '../../core/region.model';
import { RegionService } from '../../core/region.service';
import { ERROR_MESSAGE_MS, SUCCESS_MESSAGE_MS, TransientMessageService } from '../../core/transient-message.service';
import { RegionListComponent } from '../../widgets/region-list/region-list.component';

@Component({
  selector: 'app-admin-regions-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RegionListComponent],
  templateUrl: './admin-regions-page.component.html',
  styleUrl: './admin-regions-page.component.css',
})
export class AdminRegionsPageComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly regionService = inject(RegionService);
  private readonly transientMessages = inject(TransientMessageService);
  readonly refreshToken = signal(0);

  saving = false;
  deleting = false;
  selected: Region | null = null;
  showModal = false;
  showDeleteDialog = false;
  deleteCandidate: Region | null = null;
  errorMessage = '';
  toastMessage = '';

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    code: ['', [Validators.required, Validators.maxLength(20)]],
  });

  ngOnInit(): void {}

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  markRefreshed(): void {
    this.refreshToken.update((value) => value + 1);
  }

  openCreate(): void {
    this.selected = null;
    this.transientMessages.clearField(this, 'errorMessage');
    this.showModal = true;
    this.form.reset({ name: '', code: '' });
  }

  openEdit(region: Region): void {
    this.selected = region;
    this.transientMessages.clearField(this, 'errorMessage');
    this.showModal = true;
    this.form.reset({ name: region.name, code: region.code });
  }

  closeModal(): void {
    this.showModal = false;
    this.selected = null;
    this.transientMessages.clearField(this, 'errorMessage');
    this.form.reset({ name: '', code: '' });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.transientMessages.clearField(this, 'errorMessage');
    const payload = {
      name: this.form.controls.name.value.trim(),
      code: this.form.controls.code.value.trim(),
    };

    const request = this.selected ? this.regionService.update(this.selected.id, payload) : this.regionService.create(payload);

    request.subscribe({
      next: () => {
        const message = this.selected ? 'Region updated successfully.' : 'Region created successfully.';
        this.saving = false;
        this.closeModal();
        this.markRefreshed();
        this.showToast(message, SUCCESS_MESSAGE_MS);
      },
      error: (error) => {
        this.saving = false;
        const message = error?.error?.error ?? 'Failed to save region.';
        if (error?.status === 409) {
          this.transientMessages.setField(this, 'errorMessage', 'Region with that name or code already exists.', ERROR_MESSAGE_MS);
          return;
        }
        this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS);
      },
    });
  }

  requestDelete(region: Region): void {
    this.deleteCandidate = region;
    this.showDeleteDialog = true;
  }

  cancelDelete(): void {
    this.deleteCandidate = null;
    this.showDeleteDialog = false;
  }

  confirmDelete(): void {
    if (!this.deleteCandidate) {
      return;
    }

    this.deleting = true;
    const regionId = this.deleteCandidate.id;
    this.regionService.delete(regionId).subscribe({
      next: () => {
        this.deleting = false;
        this.cancelDelete();
        this.markRefreshed();
        this.showToast('Region deleted successfully.', SUCCESS_MESSAGE_MS);
      },
      error: (error) => {
        this.deleting = false;
        this.cancelDelete();
        if (error?.status === 422) {
          this.showToast('Region cannot be deleted because it is used by active users or pricelists.');
          return;
        }
        this.showToast('Region deletion failed.');
      },
    });
  }

  showToast(message: string, durationMs = ERROR_MESSAGE_MS): void {
    this.transientMessages.setField(this, 'toastMessage', message, durationMs);
  }

  controlError(controlName: 'name' | 'code'): string {
    const control = this.form.controls[controlName];
    if (!control.touched && !control.dirty) {
      return '';
    }
    if (control.hasError('required')) {
      return 'This field is required.';
    }
    if (control.hasError('maxlength')) {
      return controlName === 'name' ? 'Name can be at most 120 characters.' : 'Code can be at most 20 characters.';
    }
    return '';
  }
}
