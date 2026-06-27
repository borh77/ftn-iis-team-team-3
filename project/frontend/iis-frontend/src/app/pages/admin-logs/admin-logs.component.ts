import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import {
  ActivityLogFilters,
  AnalyticsService,
  PricelistActivityLog,
} from '../../core/analytics.service';
import { SpringPage } from '../../core/auth/auth.models';

@Component({
  selector: 'app-admin-logs',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-logs.component.html',
  styleUrl: './admin-logs.component.css',
})
export class AdminLogsComponent implements OnInit {
  private readonly analyticsService = inject(AnalyticsService);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly pageSize = 10;
  readonly defaultSort = 'timestamp,desc';

  // TODO: Replace numeric IDs with real team/user selects once admin lookup endpoints are available.
  readonly filtersForm = this.fb.nonNullable.group({
    teamId: [''],
    userId: [''],
    from: [''],
    to: [''],
  });

  loading = false;
  errorMessage = '';
  page = 0;
  data: SpringPage<PricelistActivityLog> | null = null;

  ngOnInit(): void {
    this.loadLogs(0);
  }

  applyFilters(): void {
    this.loadLogs(0);
  }

  resetFilters(): void {
    this.filtersForm.reset({
      teamId: '',
      userId: '',
      from: '',
      to: '',
    });
    this.loadLogs(0);
  }

  previousPage(): void {
    if (this.page === 0) {
      return;
    }
    this.loadLogs(this.page - 1);
  }

  nextPage(): void {
    if (!this.data || this.data.last) {
      return;
    }
    this.loadLogs(this.page + 1);
  }

  formatTimestamp(timestamp: string): string {
    return new Intl.DateTimeFormat('sr-RS', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(timestamp));
  }

  private loadLogs(page: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.analyticsService
      .getActivityLogs(this.buildFilters(), page, this.pageSize, this.defaultSort)
      .subscribe({
        next: (response) => {
          this.loading = false;
          this.page = response.number;
          this.data = {
            ...response,
            content: [...response.content],
          };
          this.cdr.detectChanges();
        },
        error: () => {
          this.loading = false;
          this.errorMessage = 'Unable to load activity logs.';
          this.data = null;
          this.cdr.detectChanges();
        },
      });
  }

  private buildFilters(): ActivityLogFilters {
    const value = this.filtersForm.getRawValue();

    return {
      teamId: this.parseNumber(value.teamId),
      userId: this.parseNumber(value.userId),
      from: this.toIsoDateTime(value.from),
      to: this.toIsoDateTime(value.to),
    };
  }

  private parseNumber(value: string): number | null {
    const normalized = value.trim();
    if (!normalized) {
      return null;
    }

    const parsed = Number(normalized);
    return Number.isFinite(parsed) ? parsed : null;
  }

  private toIsoDateTime(value: string): string | null {
    if (!value) {
      return null;
    }

    return new Date(value).toISOString();
  }
}
