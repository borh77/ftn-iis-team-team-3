import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  AnalyticsService,
  MonthlyPerformancePoint,
  PerformanceReportFilters,
  TeamPerformanceReport,
} from '../../core/analytics.service';
import {
  AdminFilterOptionsService,
  AdminLookupOption,
} from '../../core/admin-filter-options.service';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../core/transient-message.service';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-reports.component.html',
  styleUrl: './admin-reports.component.css',
})
export class AdminReportsComponent implements OnInit, OnDestroy {
  private readonly analyticsService = inject(AnalyticsService);
  private readonly filterOptionsService = inject(AdminFilterOptionsService);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly transientMessages = inject(TransientMessageService);

  readonly form = this.fb.nonNullable.group({
    teamId: [''],
    start: ['', Validators.required],
    end: ['', Validators.required],
  });

  loading = false;
  lookupLoading = false;
  pdfDownloading = false;
  errorMessage = '';
  lookupErrorMessage = '';
  pdfErrorMessage = '';
  submitted = false;
  report: TeamPerformanceReport | null = null;
  teamOptions: AdminLookupOption[] = [];

  ngOnInit(): void {
    this.resetDateRange();
    this.loadFilterOptions();
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  generateReport(): void {
    this.submitted = true;
    this.clearError();
    this.clearPdfError();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.analyticsService.getPerformanceReport(this.buildFilters()).subscribe({
      next: (report) => {
        this.loading = false;
        this.report = report;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.report = null;
        this.showError('Unable to generate the report for the selected period.');
        this.cdr.detectChanges();
      },
    });
  }

  downloadPdf(): void {
    this.submitted = true;
    this.clearPdfError();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.pdfDownloading = true;
    this.analyticsService.downloadPerformanceReportPdf(this.buildFilters()).subscribe({
      next: (blob) => {
        this.pdfDownloading = false;
        this.savePdf(blob);
        this.cdr.detectChanges();
      },
      error: () => {
        this.pdfDownloading = false;
        this.showPdfError('Unable to download the PDF report for the selected period.');
        this.cdr.detectChanges();
      },
    });
  }

  showEmptyState(): boolean {
    return !this.loading && this.submitted && !this.errorMessage && !!this.report && this.report.activatedPricelistsCount === 0;
  }

  resetFilters(): void {
    this.submitted = false;
    this.report = null;
    this.clearError();
    this.clearPdfError();
    this.form.patchValue({
      teamId: '',
    });
    this.resetDateRange();
  }

  formatHours(value: number | null | undefined): string {
    const normalized = Number(value ?? 0);
    return `${normalized.toFixed(2)} h`;
  }

  trendWidth(point: MonthlyPerformancePoint): string {
    const max = Math.max(...(this.report?.monthlyTrend ?? []).map((item) => item.averageTotalProcessingTimeHours), 0);
    if (max === 0) {
      return '0%';
    }

    return `${Math.max((point.averageTotalProcessingTimeHours / max) * 100, 6)}%`;
  }

  private buildFilters(): PerformanceReportFilters {
    const value = this.form.getRawValue();

    return {
      teamId: this.parseTeamId(),
      start: new Date(value.start).toISOString(),
      end: new Date(value.end).toISOString(),
    };
  }

  private parseTeamId(): number | null {
    const rawValue = this.form.controls.teamId.value.trim();
    if (!rawValue) {
      return null;
    }

    const parsed = Number(rawValue);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
  }

  private loadFilterOptions(): void {
    this.lookupLoading = true;
    this.lookupErrorMessage = '';

    this.filterOptionsService.getFilterOptions().subscribe({
      next: (options) => {
        this.lookupLoading = false;
        this.teamOptions = options.teams ?? [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.lookupLoading = false;
        this.lookupErrorMessage = 'Unable to load team filter options.';
        this.teamOptions = [];
        this.cdr.detectChanges();
      },
    });
  }

  private resetDateRange(): void {
    const now = new Date();
    const start = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), 1, 0, 0, 0));
    const end = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth() + 1, 0, 23, 59, 0));

    this.form.patchValue({
      start: this.toDateTimeLocal(start),
      end: this.toDateTimeLocal(end),
    });
  }

  private toDateTimeLocal(date: Date): string {
    const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 16);
  }

  private savePdf(blob: Blob): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'team-performance-report.pdf';
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  }

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private showPdfError(message: string): void {
    this.transientMessages.setField(this, 'pdfErrorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private clearError(): void {
    this.transientMessages.clearField(this, 'errorMessage', () => this.cdr.detectChanges());
  }

  private clearPdfError(): void {
    this.transientMessages.clearField(this, 'pdfErrorMessage', () => this.cdr.detectChanges());
  }
}
