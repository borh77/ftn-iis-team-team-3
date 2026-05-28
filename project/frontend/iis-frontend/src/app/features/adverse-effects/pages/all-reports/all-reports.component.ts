import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdverseEffectsApiService } from '../../api/adverse-effects-api.service';
import { AdverseEffectReport, ReportStatus } from '../../models/adverse-effect-report.model';

@Component({
  selector: 'app-all-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './all-reports.component.html',
  styleUrls: ['./all-reports.component.css']
})
export class AllReportsComponent implements OnInit {

  private readonly api = inject(AdverseEffectsApiService);

  allReports: AdverseEffectReport[] = [];
  filteredReports: AdverseEffectReport[] = [];
  loading = true;
  errorMessage = '';

  // Filter po statusu
  selectedStatus: string = '';
  statusOptions: ReportStatus[] = ['SUBMITTED', 'UNDER_REVIEW', 'CLOSED', 'EVIDENCED'];

  ngOnInit(): void {
    this.api.getAllReports().subscribe({
      next: (data) => { this.allReports = data; this.filteredReports = data; this.loading = false; },
      error: () => { this.errorMessage = 'Greška pri učitavanju naloga.'; this.loading = false; }
    });
  }

  applyFilter(): void {
    if (!this.selectedStatus) {
      this.filteredReports = this.allReports;
    } else {
      this.filteredReports = this.allReports.filter(r => r.status === this.selectedStatus);
    }
  }

  clearFilter(): void {
    this.selectedStatus = '';
    this.filteredReports = this.allReports;
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
