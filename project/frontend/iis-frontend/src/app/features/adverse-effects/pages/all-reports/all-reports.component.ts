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

  allReports: AdverseEffectReport[] = [];
  filteredReports: AdverseEffectReport[] = [];
  loading = true;
  errorMessage = '';

  selectedStatus: string = '';
  statusOptions: ReportStatus[] = ['SUBMITTED', 'UNDER_REVIEW', 'CLOSED', 'EVIDENCED'];

  ngOnInit(): void {
    this.api.getAllReports().subscribe({
      next: (data) => {
        this.allReports = data;
        this.filteredReports = data;
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
}
