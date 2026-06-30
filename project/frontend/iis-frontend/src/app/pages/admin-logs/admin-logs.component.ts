import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import {
  ActivityLogFilters,
  AnalyticsService,
  PricelistActivityLog,
} from '../../core/analytics.service';
import {
  AdminFilterOptionsService,
  AdminLookupOption,
  AdminUserLookupOption,
} from '../../core/admin-filter-options.service';
import { SpringPage } from '../../core/auth/auth.models';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../core/transient-message.service';

@Component({
  selector: 'app-admin-logs',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-logs.component.html',
  styleUrl: './admin-logs.component.css',
})
export class AdminLogsComponent implements OnInit, OnDestroy {
  private readonly analyticsService = inject(AnalyticsService);
  private readonly filterOptionsService = inject(AdminFilterOptionsService);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly transientMessages = inject(TransientMessageService);

  readonly pageSize = 10;
  readonly defaultSort = 'timestamp,desc';

  readonly filtersForm = this.fb.nonNullable.group({
    teamId: [''],
    userId: [''],
    from: [''],
    to: [''],
  });

  loading = false;
  lookupLoading = false;
  errorMessage = '';
  lookupErrorMessage = '';
  page = 0;
  data: SpringPage<PricelistActivityLog> | null = null;
  teamOptions: AdminLookupOption[] = [];
  userOptions: AdminUserLookupOption[] = [];

  ngOnInit(): void {
    this.loadFilterOptions();
    this.loadLogs(0);
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
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
    return new Intl.DateTimeFormat('en-US', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(timestamp));
  }

  displayLogDescription(log: PricelistActivityLog): string {
    const trimmed = log.description?.trim() ?? '';
    const normalized = normalizeLegacyDescription(trimmed);

    const statusMatch = LEGACY_STATUS_CHANGE_PATTERN.exec(normalized);
    if (statusMatch) {
      return `Changed status from ${statusMatch[1]} to ${statusMatch[2]}`;
    }

    if (normalized.includes(LEGACY_MARKERS.priceList) || normalized.includes(LEGACY_MARKERS.wizard)) {
      const translated = translateLegacyLog(log.actionType, normalized);
      if (translated) {
        return translated;
      }
    }

    return trimmed;
  }

  private loadLogs(page: number): void {
    this.loading = true;
    this.clearError();

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
          this.showError('Unable to load activity logs.');
          this.data = null;
          this.cdr.detectChanges();
        },
      });
  }

  private loadFilterOptions(): void {
    this.lookupLoading = true;
    this.lookupErrorMessage = '';

    this.filterOptionsService.getFilterOptions().subscribe({
      next: (options) => {
        this.lookupLoading = false;
        this.teamOptions = options.teams ?? [];
        this.userOptions = options.users ?? [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.lookupLoading = false;
        this.lookupErrorMessage = 'Unable to load team and user filter options.';
        this.teamOptions = [];
        this.userOptions = [];
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

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private clearError(): void {
    this.transientMessages.clearField(this, 'errorMessage', () => this.cdr.detectChanges());
  }
}

const LEGACY_MARKERS = {
  priceList: fromCodes(99, 101, 110, 111, 118, 110, 105, 107),
  wizard: 'wizard',
  completed: fromCodes(75, 111, 109, 112, 108, 101, 116, 105, 114, 97, 110),
  metadata: fromCodes(111, 115, 110, 111, 118, 110, 105),
  statusChangePrefix: fromCodes(80, 114, 111, 109, 101, 110, 106, 101, 110, 32, 115, 116, 97, 116, 117, 115, 32, 105, 122),
};

const LEGACY_STATUS_CHANGE_PATTERN = new RegExp(`^${LEGACY_MARKERS.statusChangePrefix} ([A-Z_]+) u ([A-Z_]+)$`);

function translateLegacyLog(actionType: string, normalized: string): string {
  switch (actionType) {
    case 'CREATE':
      return normalized.includes(LEGACY_MARKERS.wizard)
        ? 'Started pricelist creation wizard'
        : 'Created pricelist in DRAFT status';
    case 'CREATE_VERSION':
      return 'Created new pricelist version';
    case 'REPLACE_ITEM':
      return 'Replaced pricelist item';
    case 'UPDATE_ITEMS':
      return normalized.includes(LEGACY_MARKERS.wizard)
        ? 'Updated pricelist wizard items'
        : 'Updated pricelist items';
    case 'UPDATE_THRESHOLDS':
      return normalized.includes(LEGACY_MARKERS.wizard)
        ? 'Updated pricelist wizard price thresholds'
        : 'Updated pricelist price thresholds';
    case 'UPDATE_METADATA':
      if (normalized.includes(LEGACY_MARKERS.completed)) {
        return 'Completed pricelist creation wizard';
      }
      return normalized.includes(LEGACY_MARKERS.wizard) || normalized.includes(LEGACY_MARKERS.metadata)
        ? 'Updated pricelist wizard basic information'
        : 'Updated pricelist metadata';
    default:
      return '';
  }
}

function normalizeLegacyDescription(description: string): string {
  return description
    .replace(/[\u0111]/g, 'dj')
    .replace(/[\u0110]/g, 'Dj')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '');
}

function fromCodes(...codes: number[]): string {
  return String.fromCharCode(...codes);
}
