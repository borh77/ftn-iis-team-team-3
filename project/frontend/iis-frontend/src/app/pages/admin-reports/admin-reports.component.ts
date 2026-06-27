import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  AnalyticsService,
  MonthlyPerformancePoint,
  PerformanceReportFilters,
  TeamPerformanceReport,
} from '../../core/analytics.service';

type TeamMode = 'all' | 'specific';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-reports.component.html',
  styleUrl: './admin-reports.component.css',
})
export class AdminReportsComponent implements OnInit {
  private readonly analyticsService = inject(AnalyticsService);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);

  // TODO: Replace the specific-team numeric field with a real team dropdown once an admin teams lookup endpoint exists.
  readonly form = this.fb.nonNullable.group({
    teamMode: ['all' as TeamMode, Validators.required],
    teamId: [''],
    start: ['', Validators.required],
    end: ['', Validators.required],
  });

  loading = false;
  pdfDownloading = false;
  errorMessage = '';
  pdfErrorMessage = '';
  submitted = false;
  report: TeamPerformanceReport | null = null;

  ngOnInit(): void {
    const now = new Date();
    const start = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), 1, 0, 0, 0));
    const end = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth() + 1, 0, 23, 59, 0));

    this.form.patchValue({
      start: this.toDateTimeLocal(start),
      end: this.toDateTimeLocal(end),
    });
  }

  generateReport(): void {
    this.submitted = true;
    this.errorMessage = '';
    this.pdfErrorMessage = '';

    if (this.form.invalid || !this.hasValidTeamFilter()) {
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
        this.errorMessage = 'Unable to generate the report for the selected period.';
        this.cdr.detectChanges();
      },
    });
  }

  downloadPdf(): void {
    this.submitted = true;
    this.pdfErrorMessage = '';

    if (this.form.invalid || !this.hasValidTeamFilter()) {
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
        this.pdfErrorMessage = 'Unable to download the PDF report for the selected period.';
        this.cdr.detectChanges();
      },
    });
  }

  isSpecificTeam(): boolean {
    return this.form.controls.teamMode.value === 'specific';
  }

  showEmptyState(): boolean {
    return !this.loading && this.submitted && !this.errorMessage && !!this.report && this.report.activatedPricelistsCount === 0;
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

  private hasValidTeamFilter(): boolean {
    if (!this.isSpecificTeam()) {
      return true;
    }

    return this.parseTeamId() !== null;
  }

  private buildFilters(): PerformanceReportFilters {
    const value = this.form.getRawValue();

    return {
      teamId: this.isSpecificTeam() ? this.parseTeamId() : null,
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

  private toDateTimeLocal(date: Date): string {
    const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 16);
  }

  private savePdf(blob: Blob): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'team-performance-report.pdf';
    link.click();
    window.URL.revokeObjectURL(url);
  }
}
