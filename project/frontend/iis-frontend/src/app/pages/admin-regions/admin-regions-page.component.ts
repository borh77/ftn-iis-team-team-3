import { CommonModule } from '@angular/common';
import { Component, signal, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Region } from '../../core/region.model';
import { RegionService } from '../../core/region.service';
import { RegionListComponent } from '../../widgets/region-list/region-list.component';

@Component({
  selector: 'app-admin-regions-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RegionListComponent],
  templateUrl: './admin-regions-page.component.html',
  styleUrl: './admin-regions-page.component.css',
})
export class AdminRegionsPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly regionService = inject(RegionService);
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

  ngOnInit(): void {
  }

  markRefreshed(): void {
    this.refreshToken.update((value) => value + 1);
  }

  openCreate(): void {
    this.selected = null;
    this.errorMessage = '';
    this.showModal = true;
    this.form.reset({ name: '', code: '' });
  }

  openEdit(region: Region): void {
    this.selected = region;
    this.errorMessage = '';
    this.showModal = true;
    this.form.reset({ name: region.name, code: region.code });
  }

  closeModal(): void {
    this.showModal = false;
    this.selected = null;
    this.errorMessage = '';
    this.form.reset({ name: '', code: '' });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    const payload = {
      name: this.form.controls.name.value.trim(),
      code: this.form.controls.code.value.trim(),
    };

    const request = this.selected ? this.regionService.update(this.selected.id, payload) : this.regionService.create(payload);

    request.subscribe({
      next: () => {
        this.saving = false;
        this.closeModal();
        this.markRefreshed();
      },
      error: (error) => {
        this.saving = false;
        const message = error?.error?.error ?? 'Neuspešno čuvanje regiona.';
        if ((error?.status === 409) || message.includes('Region sa tim imenom ili kodom već postoji')) {
          this.errorMessage = 'Region sa tim imenom ili kodom već postoji';
          return;
        }
        this.errorMessage = message;
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
      },
      error: (error) => {
        this.deleting = false;
        this.cancelDelete();
        if (error?.status === 422) {
          this.showToast('Nije moguće obrisati region jer ga koriste aktivni korisnici ili cenovnici');
          return;
        }
        this.showToast('Brisanje regiona nije uspelo.');
      },
    });
  }

  showToast(message: string): void {
    this.toastMessage = message;
    window.setTimeout(() => {
      if (this.toastMessage === message) {
        this.toastMessage = '';
      }
    }, 3500);
  }

  controlError(controlName: 'name' | 'code'): string {
    const control = this.form.controls[controlName];
    if (!control.touched && !control.dirty) {
      return '';
    }
    if (control.hasError('required')) {
      return 'Polje je obavezno.';
    }
    if (control.hasError('maxlength')) {
      return controlName === 'name' ? 'Naziv može imati najviše 120 karaktera.' : 'Kod može imati najviše 20 karaktera.';
    }
    return '';
  }
}