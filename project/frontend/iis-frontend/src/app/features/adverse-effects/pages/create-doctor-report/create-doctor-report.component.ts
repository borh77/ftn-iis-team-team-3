import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AdverseEffectsApiService } from '../../api/adverse-effects-api.service';
import { CreateDoctorReportRequest } from '../../models/adverse-effect-report.model';

@Component({
  selector: 'app-create-doctor-report',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  templateUrl: './create-doctor-report.component.html',
  styleUrls: ['./create-doctor-report.component.css']
})
export class CreateDoctorReportComponent {

  private readonly api = inject(AdverseEffectsApiService);
  private readonly router = inject(Router);

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

  submit(): void {
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.api.createDoctorReport(this.form).subscribe({
      next: (report) => {
        this.saving = false;
        // Redirect to my-reports with success message passed via router state
        this.router.navigate(['/adverse-effects/my-reports'], {
          state: { successMessage: `Report #${report.id} created successfully! Status: ${report.status}` }
        });
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = 'Error creating report. Please check your input.';
        console.error(err);
      }
    });
  }

  goToMyReports(): void {
    this.router.navigate(['/adverse-effects/my-reports']);
  }
}
