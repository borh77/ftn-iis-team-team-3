import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { SalesApiService, SalesAnalyticsSummary } from '../../features/sales/api/sales-api.service';

@Component({
  selector: 'app-sales-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './sales-dashboard.component.html',
  styleUrls: ['./sales-dashboard.component.css'],
})
export class SalesDashboardComponent implements OnInit {
  private readonly salesApiService = inject(SalesApiService);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = true;
  summary: SalesAnalyticsSummary | null = null;

  ngOnInit(): void {
    this.loadSummary();
  }

  loadSummary(): void {
    this.loading = true;

    this.salesApiService.getSalesAnalyticsSummary().subscribe({
      next: (response) => {
        this.summary = response;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load sales analytics summary:', error);
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  mapEntries(map?: Record<string, number>): { key: string; value: number }[] {
    return Object.entries(map ?? {}).map(([key, value]) => ({ key, value }));
  }

  calculateRate(part: number | undefined, total: number | undefined): number {
    if (!part || !total) {
      return 0;
    }

    return Math.round((part / total) * 100);
  }

  downloadReport(): void {
    this.salesApiService.downloadSalesAnalyticsReport().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');

        link.href = url;
        link.download = 'sales-analytics-report.pdf';
        link.click();

        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Failed to download sales analytics report:', error);
      },
    });
  }
}