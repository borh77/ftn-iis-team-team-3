import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { finalize } from 'rxjs';
import { AdverseEffectsApiService } from '../../api/adverse-effects-api.service';
import { extractBackendErrorMessage } from '../../../../core/http-error-message';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';

@Component({
  selector: 'app-edit-report',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  templateUrl: './edit-report.component.html',
  styleUrls: ['./edit-report.component.css']
})
export class EditReportComponent implements OnInit, OnDestroy {

  private readonly api = inject(AdverseEffectsApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly transientMessages = inject(TransientMessageService);

  loading = true;
  saving = false;
  errorMessage = '';

  severityOptions = ['MILD', 'MODERATE', 'SEVERE', 'CRITICAL'];
  genderOptions = ['Male', 'Female', 'Other'];
  today = new Date().toISOString().split('T')[0];
  reportId!: number;

  form = {
    medicationName: '',
    severity: '',
    source: 'Web',
    symptomDate: '',
    effectDescription: '',
    additionalNotes: '',
    patientGender: '',
    patientAge: undefined as number | undefined
  };

  ngOnInit(): void {
    this.reportId = Number(this.route.snapshot.paramMap.get('id'));
    this.api.getReportById(this.reportId).pipe(finalize(() => {
      this.loading = false;
      this.cdr.detectChanges();
    })).subscribe({
      next: (report) => {
        this.form.medicationName = report.medicationName;
        this.form.severity = report.severity ?? '';
        this.form.symptomDate = report.symptomDate ?? '';
        this.form.effectDescription = report.effectDescription ?? '';
        this.form.additionalNotes = report.additionalNotes ?? '';
        this.form.patientGender = report.patientGender ?? '';
        this.form.patientAge = report.patientAge;
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Error loading report.'));
        this.cdr.detectChanges();
      }
    });
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  submit(): void {
    this.saving = true;
    this.clearError();
    this.api.updateDoctorReport(this.reportId, this.form).pipe(finalize(() => {
      this.saving = false;
      this.cdr.detectChanges();
    })).subscribe({
      next: () => {
        this.router.navigate(['/adverse-effects/my-reports'], {
          state: { successMessage: `Report #${this.reportId} updated successfully!` }
        });
      },
      error: (err) => {
        this.showError(extractBackendErrorMessage(err, 'Error updating report. Please try again.'));
        this.cdr.detectChanges();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/adverse-effects/my-reports']);
  }

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private clearError(): void {
    this.transientMessages.clearField(this, 'errorMessage', () => this.cdr.detectChanges());
  }
}
