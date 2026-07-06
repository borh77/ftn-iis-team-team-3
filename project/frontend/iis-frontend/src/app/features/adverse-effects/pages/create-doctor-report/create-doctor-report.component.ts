import { ChangeDetectorRef, Component, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { finalize } from 'rxjs';
import { AdverseEffectsApiService } from '../../api/adverse-effects-api.service';
import { CreateDoctorReportRequest } from '../../models/adverse-effect-report.model';
import { extractBackendErrorMessage } from '../../../../core/http-error-message';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';

@Component({
  selector: 'app-create-doctor-report',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  templateUrl: './create-doctor-report.component.html',
  styleUrls: ['./create-doctor-report.component.css']
})
export class CreateDoctorReportComponent implements OnDestroy {

  private readonly api = inject(AdverseEffectsApiService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly transientMessages = inject(TransientMessageService);

  saving = false;
  errorMessage = '';
  successMessage = '';

  severityOptions = ['MILD', 'MODERATE', 'SEVERE', 'CRITICAL'];
  genderOptions = ['Male', 'Female', 'Other'];
  today = new Date().toISOString().split('T')[0];

  form: CreateDoctorReportRequest = {
    medicationName: '',
    severity: '',
    source: 'Web',
    symptomDate: '',
    effectDescription: '',
    additionalNotes: '',
    patientGender: '',
    patientAge: undefined
  };

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  submit(): void {
    this.saving = true;
    this.transientMessages.clearField(this, 'errorMessage');
    this.transientMessages.clearField(this, 'successMessage');

    this.api.createDoctorReport(this.form).pipe(finalize(() => {
      this.saving = false;
      this.cdr.detectChanges();
    })).subscribe({
      next: (report) => {
        // Redirect to my-reports with success message passed via router state
        this.router.navigate(['/adverse-effects/my-reports'], {
          state: { successMessage: `Report #${report.id} created successfully! Status: ${report.status}` }
        });
      },
      error: (err) => {
        this.transientMessages.setField(
          this,
          'errorMessage',
          extractBackendErrorMessage(err, 'Error creating report. Please check your input.'),
          ERROR_MESSAGE_MS,
          () => this.cdr.detectChanges()
        );
        this.cdr.detectChanges();
      }
    });
  }

  goToMyReports(): void {
    this.router.navigate(['/adverse-effects/my-reports']);
  }
}
