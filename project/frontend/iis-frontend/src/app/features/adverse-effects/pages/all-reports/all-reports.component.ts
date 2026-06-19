import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AdverseEffectsApiService } from '../../api/adverse-effects-api.service';
import { AdverseEffectReport, ReportStatus } from '../../models/adverse-effect-report.model';

@Component({
  selector: 'app-all-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  templateUrl: './all-reports.component.html',
  styleUrls: ['./all-reports.component.css']
})
export class AllReportsComponent implements OnInit {

  private readonly api = inject(AdverseEffectsApiService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  reports: AdverseEffectReport[] = [];
  loading = true;
  errorMessage = '';

  selectedStatus = '';
  medicationName = '';
  selectedSeverity = '';
  statusOptions: ReportStatus[] = ['SUBMITTED', 'UNDER_REVIEW', 'CLOSED', 'EVIDENCED'];
  severityOptions = ['MILD', 'MODERATE', 'SEVERE', 'LIFE_THREATENING', 'CRITICAL'];

  ngOnInit(): void {
    this.loadReports();
  }

  applyFilter(): void {
    this.loadReports({
      status: this.selectedStatus,
      medicationName: this.medicationName.trim(),
      severity: this.selectedSeverity
    });
  }

  clearFilter(): void {
    this.selectedStatus = '';
    this.medicationName = '';
    this.selectedSeverity = '';
    this.loadReports();
  }

  viewReport(id: number): void {
    this.router.navigate(['/adverse-effects/report', id]);
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

  private loadReports(filters: { status?: string; medicationName?: string; severity?: string } = {}): void {
    this.loading = true;
    this.errorMessage = '';

    this.api.getAllReportsFiltered(filters).subscribe({
      next: (data) => {
        this.reports = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Error loading reports.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
