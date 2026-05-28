import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdverseEffectsApiService } from '../../api/adverse-effects-api.service';
import { CreateDoctorReportRequest } from '../../models/adverse-effect-report.model';

@Component({
  selector: 'app-create-doctor-report',
  standalone: true,
  imports: [CommonModule, FormsModule],
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
  sourceOptions = ['web', 'mobile', 'api'];

  form: CreateDoctorReportRequest = {
    medicationName: '',
    severity: '',
    source: 'web',
    symptomDate: '',
    effectDescription: '',
    additionalNotes: ''
  };

  submit(): void {
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.api.createDoctorReport(this.form).subscribe({
      next: (report) => {
        this.saving = false;
        this.successMessage = `Nalog #${report.id} uspešno kreiran! Status: ${report.status}`;
        // Reset forme
        this.form = {
          medicationName: '',
          severity: '',
          source: 'web',
          symptomDate: '',
          effectDescription: '',
          additionalNotes: ''
        };
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = 'Greška pri kreiranju naloga. Proverite unos.';
        console.error(err);
      }
    });
  }

  goToMyReports(): void {
    this.router.navigate(['/adverse-effects/my-reports']);
  }
}
