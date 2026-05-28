import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AdverseEffectsApiService } from '../../api/adverse-effects-api.service';
import { AdverseEffectReport } from '../../models/adverse-effect-report.model';

@Component({
  selector: 'app-my-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-reports.component.html',
  styleUrls: ['./my-reports.component.css']
})
export class MyReportsComponent implements OnInit {

  private readonly api = inject(AdverseEffectsApiService);
  private readonly router = inject(Router);

  reports: AdverseEffectReport[] = [];
  loading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.api.getMyReports().subscribe({
      next: (data) => { this.reports = data; this.loading = false; },
      error: () => { this.errorMessage = 'Greška pri učitavanju naloga.'; this.loading = false; }
    });
  }

  // Editovanje je dostupno samo dok je status SUBMITTED
  canEdit(report: AdverseEffectReport): boolean {
    return report.status === 'SUBMITTED';
  }

  goToCreate(): void {
    this.router.navigate(['/adverse-effects/create-doctor-report']);
  }

  goToEdit(id: number): void {
    this.router.navigate(['/adverse-effects/edit-report', id]);
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      'SUBMITTED': 'status-submitted',
      'UNDER_REVIEW': 'status-review',
      'CLOSED': 'status-closed',
      'EVIDENCED': 'status-evidenced'
    };
    return map[status] ?? '';
  }
}
