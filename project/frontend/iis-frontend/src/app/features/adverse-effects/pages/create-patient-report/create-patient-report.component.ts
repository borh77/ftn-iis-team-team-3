import { ChangeDetectorRef, Component, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdverseEffectsApiService } from '../../api/adverse-effects-api.service';
import { CreatePatientReportRequest } from '../../models/adverse-effect-report.model';
import { ERROR_MESSAGE_MS, SUCCESS_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';

interface MedicationSymptoms {
  [medication: string]: string[];
}

@Component({
  selector: 'app-create-patient-report',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-patient-report.component.html',
  styleUrls: ['./create-patient-report.component.css']
})
export class CreatePatientReportComponent implements OnDestroy {

  private readonly api = inject(AdverseEffectsApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly transientMessages = inject(TransientMessageService);

  saving = false;
  errorMessage = '';
  successMessage = '';
  today = new Date().toISOString().split('T')[0];

  genderOptions = ['Male', 'Female', 'Other'];

  medications = [
    'Aspirin', 'Ibuprofen', 'Paracetamol', 'Amoxicillin', 'Metformin',
    'Atorvastatin', 'Omeprazole', 'Lisinopril', 'Amlodipine', 'Sertraline',
    'Azithromycin', 'Ciprofloxacin', 'Prednisone', 'Warfarin', 'Metoprolol'
  ];

  symptomMap: MedicationSymptoms = {
    'Aspirin':        ['Stomach pain', 'Nausea', 'Heartburn', 'Bleeding', 'Ringing in ears', 'Dizziness'],
    'Ibuprofen':      ['Stomach pain', 'Nausea', 'Heartburn', 'Headache', 'Dizziness', 'Rash'],
    'Paracetamol':    ['Nausea', 'Liver pain', 'Skin rash', 'Itching', 'Jaundice'],
    'Amoxicillin':    ['Diarrhea', 'Rash', 'Nausea', 'Vomiting', 'Allergic reaction', 'Itching'],
    'Metformin':      ['Nausea', 'Diarrhea', 'Stomach pain', 'Loss of appetite', 'Metallic taste'],
    'Atorvastatin':   ['Muscle pain', 'Muscle weakness', 'Headache', 'Nausea', 'Joint pain', 'Diarrhea'],
    'Omeprazole':     ['Headache', 'Diarrhea', 'Nausea', 'Stomach pain', 'Constipation', 'Dizziness'],
    'Lisinopril':     ['Dry cough', 'Dizziness', 'Headache', 'Fatigue', 'High potassium', 'Rash'],
    'Amlodipine':     ['Swollen ankles', 'Flushing', 'Headache', 'Dizziness', 'Fatigue', 'Nausea'],
    'Sertraline':     ['Nausea', 'Insomnia', 'Diarrhea', 'Dizziness', 'Dry mouth', 'Sweating'],
    'Azithromycin':   ['Nausea', 'Diarrhea', 'Stomach pain', 'Vomiting', 'Rash', 'Heart palpitations'],
    'Ciprofloxacin':  ['Nausea', 'Diarrhea', 'Headache', 'Dizziness', 'Rash', 'Tendon pain'],
    'Prednisone':     ['Weight gain', 'Mood changes', 'Insomnia', 'Increased appetite', 'High blood sugar', 'Swelling'],
    'Warfarin':       ['Bleeding', 'Bruising', 'Nausea', 'Hair loss', 'Skin rash', 'Dizziness'],
    'Metoprolol':     ['Fatigue', 'Dizziness', 'Cold hands', 'Slow heartbeat', 'Shortness of breath', 'Nausea'],
  };

  availableSymptoms: string[] = [];
  selectedSymptoms: string[] = [];

  form: CreatePatientReportRequest = {
    medicationName: '',
    symptoms: '',
    additionalDesc: '',
    patientGender: '',
    patientAge: undefined,
    symptomDate: ''
  };

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  onMedicationChange(): void {
    this.selectedSymptoms = [];
    this.availableSymptoms = this.symptomMap[this.form.medicationName] ?? [];
    this.form.symptoms = '';
    this.cdr.detectChanges();
  }

  toggleSymptom(symptom: string): void {
    const idx = this.selectedSymptoms.indexOf(symptom);
    if (idx >= 0) {
      this.selectedSymptoms.splice(idx, 1);
    } else {
      this.selectedSymptoms.push(symptom);
    }
    this.form.symptoms = this.selectedSymptoms.join(', ');
  }

  isSelected(symptom: string): boolean {
    return this.selectedSymptoms.includes(symptom);
  }

  submit(): void {
    if (!this.form.medicationName || !this.form.symptoms || !this.form.symptomDate) {
      this.showError('Please fill in all required fields.');
      return;
    }

    this.saving = true;
    this.clearResultMessages();

    this.api.createPatientReport(this.form).subscribe({
      next: (report) => {
        this.saving = false;
        this.showSuccess('Thank you for submitting your report! We appreciate you taking the time to inform us. Your report has been recorded and we will take it into consideration.');
        this.form = { medicationName: '', symptoms: '', additionalDesc: '', patientGender: '', patientAge: undefined, symptomDate: '' };
        this.selectedSymptoms = [];
        this.availableSymptoms = [];
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.saving = false;
        this.showError('Error submitting report. Please try again.');
        console.error(err);
        this.cdr.detectChanges();
      }
    });
  }

  private showSuccess(message: string): void {
    this.transientMessages.setField(this, 'successMessage', message, SUCCESS_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private clearResultMessages(): void {
    this.transientMessages.clearField(this, 'successMessage', () => this.cdr.detectChanges());
    this.transientMessages.clearField(this, 'errorMessage', () => this.cdr.detectChanges());
  }
}
