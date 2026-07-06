import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { catchError, finalize, of, timeout } from 'rxjs';
import { AdverseEffectsApiService } from '../../api/adverse-effects-api.service';
import {
  AdverseEffectAnalyticsSummary,
  AnalyticsCountItem
} from '../../models/adverse-effect-report.model';

@Component({
  selector: 'app-adverse-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  templateUrl: './adverse-analytics.component.html',
  styleUrls: ['./adverse-analytics.component.css']
})
export class AdverseAnalyticsComponent implements OnInit {
  private readonly api = inject(AdverseEffectsApiService);

  summary: AdverseEffectAnalyticsSummary | null = null;
  loading = false;
  pdfDownloading = false;
  errorMessage = '';
  pdfErrorMessage = '';
  refreshNotice = '';
  analystInterpretation = '';
  lastUpdatedAt = '';

  private requestSerial = 0;
  private safetyTimerId: number | null = null;

  ngOnInit(): void {
    this.loadAnalytics();
  }

  loadAnalytics(): void {
    const requestId = ++this.requestSerial;
    this.loading = true;
    this.errorMessage = '';
    this.pdfErrorMessage = '';
    this.refreshNotice = 'Refreshing analytics...';
    this.clearSafetyTimer();

    this.safetyTimerId = window.setTimeout(() => {
      if (this.requestSerial === requestId && this.loading) {
        this.loading = false;
        this.refreshNotice = '';
        this.errorMessage = 'Refresh timed out. Showing the latest loaded statistics.';
        if (!this.summary) {
          this.summary = this.emptySummary();
        }
      }
    }, 2500);

    this.api.getAnalyticsSummary()
      .pipe(timeout(3000))
      .pipe(catchError((error) => {
        if (this.requestSerial === requestId) {
          this.errorMessage = error?.error?.error ?? 'Unable to load adverse effect analytics.';
          if (!this.summary) {
            this.summary = this.emptySummary();
          }
        }
        return of(null);
      }))
      .pipe(finalize(() => {
        if (this.requestSerial === requestId) {
          this.clearSafetyTimer();
          this.loading = false;
          if (!this.errorMessage) {
            this.refreshNotice = '';
          }
        }
      }))
      .subscribe({
        next: (summary) => {
          if (!summary || this.requestSerial !== requestId) {
            return;
          }
          this.summary = summary;
          this.errorMessage = '';
          this.refreshNotice = '';
          this.lastUpdatedAt = new Date().toLocaleString();
        }
      });
  }

  refreshAnalytics(): void {
    this.loadAnalytics();
  }

  generatePdfReport(): void {
    if (!this.canGeneratePdf) {
      this.pdfErrorMessage = 'Write analyst interpretation before generating the PDF report.';
      return;
    }

    this.pdfDownloading = true;
    this.pdfErrorMessage = '';

    this.api.downloadAnalyticsPdf({ analystInterpretation: this.analystInterpretation.trim() })
      .pipe(finalize(() => (this.pdfDownloading = false)))
      .subscribe({
        next: (blob) => this.savePdf(blob),
        error: () => {
          this.pdfErrorMessage = 'Unable to generate PDF report for selected analytics filters.';
        }
      });
  }

  get canGeneratePdf(): boolean {
    return this.analystInterpretation.trim().length > 0;
  }

  maxCount(items: AnalyticsCountItem[] | undefined): number {
    if (!items?.length) {
      return 0;
    }
    return Math.max(...items.map((item) => item.count));
  }

  barWidth(count: number, max: number): number {
    if (!max) {
      return 0;
    }
    return Math.max(6, Math.round((count / max) * 100));
  }

  percent(part: number, total: number): number {
    if (!total) {
      return 0;
    }
    return Math.round((part * 1000) / total) / 10;
  }

  topItems(items: AnalyticsCountItem[] | undefined, limit = 5): AnalyticsCountItem[] {
    return items?.slice(0, limit) ?? [];
  }

  topItem(items: AnalyticsCountItem[] | undefined): AnalyticsCountItem | null {
    return items?.length ? items[0] : null;
  }

  sourceDonut(summary: AdverseEffectAnalyticsSummary): string {
    if (!summary.totalReports) {
      return 'conic-gradient(#dbe2ee 0 100%)';
    }

    const doctorShare = this.percent(summary.doctorReports, summary.totalReports);
    return `conic-gradient(#2563eb 0 ${doctorShare}%, #0f9f8f ${doctorShare}% 100%)`;
  }

  statusLabel(status: string): string {
    return status
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  signalNarrative(summary: AdverseEffectAnalyticsSummary): string {
    const medication = this.topItem(summary.reportsByMedication);
    const effect = this.topItem(summary.reportsByEffect);

    if (!summary.totalReports || !medication) {
      return 'No report data is available yet for signal review.';
    }

    if (!effect) {
      return `${medication.label} currently has the highest report volume and should be reviewed by the analyst.`;
    }

    return `${medication.label} leads the dataset, while ${effect.label} is the most frequent recorded effect.`;
  }

  statusClass(label: string): string {
    return label.toLowerCase().replace(/_/g, '-');
  }

  private emptySummary(): AdverseEffectAnalyticsSummary {
    return {
      totalReports: 0,
      doctorReports: 0,
      patientReports: 0,
      submittedReports: 0,
      underReviewReports: 0,
      closedReports: 0,
      evidencedReports: 0,
      reportsByMedication: [],
      reportsByEffect: [],
      reportsByStatus: [],
      reportsByReporterType: [],
      reportsOverTime: []
    };
  }

  private savePdf(blob: Blob): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `adverse-effect-analytics-report-${new Date().toISOString().slice(0, 10)}.pdf`;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  private clearSafetyTimer(): void {
    if (this.safetyTimerId !== null) {
      window.clearTimeout(this.safetyTimerId);
      this.safetyTimerId = null;
    }
  }
}
