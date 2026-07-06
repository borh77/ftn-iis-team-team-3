import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { catchError, finalize, of, timeout } from 'rxjs';
import { AdverseEffectsApiService } from '../../api/adverse-effects-api.service';
import {
  AdverseEffectAnalyticsSummary,
  AnalyticsCountItem,
  AnalyticsReportType,
  AnalyticsTimeBucket
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
  private readonly cdr = inject(ChangeDetectorRef);

  summary: AdverseEffectAnalyticsSummary | null = null;
  loading = false;
  pdfDownloadingType: AnalyticsReportType | null = null;
  errorMessage = '';
  pdfErrorMessage = '';
  refreshNotice = '';
  analystInterpretation = '';
  lastUpdatedAt = '';
  reportOptions: Array<{
    type: AnalyticsReportType;
    title: string;
    description: string;
    requiresInterpretation: boolean;
  }> = [
    {
      type: 'comprehensive',
      title: 'Comprehensive report',
      description: 'Complete management-style PDF with all pharmacovigilance statistics.',
      requiresInterpretation: true
    },
    {
      type: 'medication',
      title: 'Medicine ranking',
      description: 'Focused report for medicine signal volume and prioritization.',
      requiresInterpretation: false
    },
    {
      type: 'effects',
      title: 'Effect profile',
      description: 'Focused report for most frequent adverse effects and symptoms.',
      requiresInterpretation: false
    },
    {
      type: 'status',
      title: 'Workflow status',
      description: 'Focused report for Submitted, Under Review, Closed and Evidenced workload.',
      requiresInterpretation: false
    },
    {
      type: 'reporter',
      title: 'Reporter mix',
      description: 'Focused report comparing doctor and patient report contribution.',
      requiresInterpretation: false
    },
    {
      type: 'source',
      title: 'Submission channels',
      description: 'Focused report for web, patient portal and other intake sources.',
      requiresInterpretation: false
    },
    {
      type: 'timeline',
      title: 'Case intake trend',
      description: 'Focused report for daily case volume and workload spikes.',
      requiresInterpretation: false
    }
  ];

  private requestSerial = 0;
  private safetyTimerId: ReturnType<typeof setTimeout> | null = null;

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

    this.safetyTimerId = setTimeout(() => {
      if (this.requestSerial === requestId && this.loading) {
        this.loading = false;
        this.refreshNotice = '';
        this.errorMessage = 'Refresh timed out. Showing the latest loaded statistics.';
        if (!this.summary) {
          this.summary = this.emptySummary();
        }
        this.cdr.detectChanges();
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
          this.cdr.detectChanges();
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
          this.cdr.detectChanges();
        }
      });
  }

  refreshAnalytics(): void {
    this.loadAnalytics();
  }

  get pdfDownloading(): boolean {
    return this.pdfDownloadingType !== null;
  }

  generatePdfReport(reportType: AnalyticsReportType = 'comprehensive'): void {
    if (!this.canGenerateReport(reportType)) {
      this.pdfErrorMessage = 'Write analyst interpretation before generating the comprehensive PDF report.';
      return;
    }

    this.pdfDownloadingType = reportType;
    this.pdfErrorMessage = '';

    this.api.downloadAnalyticsPdf({
      analystInterpretation: this.analystInterpretation.trim(),
      reportType
    })
      .pipe(finalize(() => {
        this.pdfDownloadingType = null;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (blob) => this.savePdf(blob, reportType),
        error: () => {
          this.pdfErrorMessage = 'Unable to generate PDF report for selected analytics filters.';
        }
      });
  }

  isReportDownloading(reportType: AnalyticsReportType): boolean {
    return this.pdfDownloadingType === reportType;
  }

  get canGeneratePdf(): boolean {
    return this.analystInterpretation.trim().length > 0;
  }

  canGenerateReport(reportType: AnalyticsReportType): boolean {
    return !this.requiresInterpretation(reportType) || this.canGeneratePdf;
  }

  requiresInterpretation(reportType: AnalyticsReportType): boolean {
    return reportType === 'comprehensive';
  }

  reportRequirementLabel(reportType: AnalyticsReportType): string {
    return this.requiresInterpretation(reportType) ? 'Requires analyst conclusion' : 'Ready without conclusion';
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

  maxTimeCount(items: AnalyticsTimeBucket[] | undefined): number {
    if (!items?.length) {
      return 0;
    }
    return Math.max(...items.map((item) => item.count));
  }

  timeBarWidth(count: number, max: number): number {
    return this.barWidth(count, max);
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
      reportsBySource: [],
      reportsOverTime: []
    };
  }

  private savePdf(blob: Blob, reportType: AnalyticsReportType): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `adverse-effect-${reportType}-report-${new Date().toISOString().slice(0, 10)}.pdf`;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  private clearSafetyTimer(): void {
    if (this.safetyTimerId !== null) {
      clearTimeout(this.safetyTimerId);
      this.safetyTimerId = null;
    }
  }
}
