import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.token';
import { SpringPage } from './auth/auth.models';

export interface ActivityLogFilters {
  teamId?: number | null;
  userId?: number | null;
  from?: string | null;
  to?: string | null;
}

export interface PricelistActivityLog {
  id: number;
  pricelistId: number;
  userId: number;
  teamId: number | null;
  actionType: string;
  description: string;
  timestamp: string;
  statusFrom?: string | null;
  statusTo?: string | null;
}

export interface PerformanceReportFilters {
  teamId?: number | null;
  start: string;
  end: string;
}

export interface PricelistDashboardFilters {
  teamId?: number | null;
  dateFrom?: string | null;
  dateTo?: string | null;
}

export interface PricelistDashboardBreakdownItem {
  id: number | null;
  label: string;
  count: number;
}

export interface RecentPricelistSummary {
  id: number;
  regionName?: string | null;
  customerSegment?: string | null;
  status: string;
  teamName?: string | null;
  itemCount: number;
  periodStart?: string | null;
  periodEnd?: string | null;
  lastEditedAt?: string | null;
  creationCompleted: boolean;
}

export interface PricelistDashboardSummary {
  totalPricelists: number;
  draftCount: number;
  inReviewCount: number;
  activeCount: number;
  archivedCount: number;
  waitingForReviewCount: number;
  incompleteDraftCount: number;
  stuckDraftCount: number;
  stuckInReviewCount: number;
  activeOffersCount: number;
  activatedPricelistsCount: number;
  averageProcessingTimeHours: number;
  averageReviewTimeHours: number;
  statusCounts: Record<string, number>;
  pricelistsByRegion: PricelistDashboardBreakdownItem[];
  pricelistsBySegment: PricelistDashboardBreakdownItem[];
  pricelistsByTeam: PricelistDashboardBreakdownItem[];
  activityCountByActionType: PricelistDashboardBreakdownItem[];
  recentPricelists: RecentPricelistSummary[];
  recentActivity: PricelistActivityLog[];
}

export interface MonthlyPerformancePoint {
  month: string;
  averageTotalProcessingTimeHours: number;
  activatedPricelistsCount: number;
}

export interface TeamPerformanceReport {
  teamId: number | null;
  periodStart: string;
  periodEnd: string;
  averageTotalProcessingTimeHours: number;
  averageReviewTimeHours: number;
  activatedPricelistsCount: number;
  stuckDraftCount: number;
  stuckInReviewCount: number;
  monthlyTrend: MonthlyPerformancePoint[];
  teamFilterLimitation?: string | null;
}

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly http = inject(HttpClient);

  getActivityLogs(
    filters: ActivityLogFilters,
    page = 0,
    size = 10,
    sort = 'timestamp,desc',
  ): Observable<SpringPage<PricelistActivityLog>> {
    let params = this.activityLogParams(filters)
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.http.get<SpringPage<PricelistActivityLog>>(`${this.apiBaseUrl}/api/admin/logs`, {
      params,
    });
  }

  downloadActivityLogsPdf(filters: ActivityLogFilters): Observable<Blob> {
    return this.http.get(`${this.apiBaseUrl}/api/admin/activity-logs/pdf`, {
      params: this.activityLogParams(filters),
      responseType: 'blob',
    });
  }

  getPerformanceReport(filters: PerformanceReportFilters): Observable<TeamPerformanceReport> {
    const params = this.performanceReportParams(filters);

    return this.http.get<TeamPerformanceReport>(
      `${this.apiBaseUrl}/api/admin/analytics/performance`,
      { params },
    );
  }

  downloadPerformanceReportPdf(filters: PerformanceReportFilters): Observable<Blob> {
    const params = this.performanceReportParams(filters);

    return this.http.get(`${this.apiBaseUrl}/api/admin/analytics/performance/pdf`, {
      params,
      responseType: 'blob',
    });
  }

  getPricelistDashboard(filters: PricelistDashboardFilters): Observable<PricelistDashboardSummary> {
    return this.http.get<PricelistDashboardSummary>(
      `${this.apiBaseUrl}/api/admin/analytics/pricelist-dashboard`,
      { params: this.pricelistDashboardParams(filters) },
    );
  }

  private performanceReportParams(filters: PerformanceReportFilters): HttpParams {
    let params = new HttpParams().set('start', filters.start).set('end', filters.end);

    if (filters.teamId !== null && filters.teamId !== undefined) {
      params = params.set('teamId', filters.teamId.toString());
    }

    return params;
  }

  private pricelistDashboardParams(filters: PricelistDashboardFilters): HttpParams {
    let params = new HttpParams();

    if (filters.teamId !== null && filters.teamId !== undefined) {
      params = params.set('teamId', filters.teamId.toString());
    }
    if (filters.dateFrom) {
      params = params.set('dateFrom', filters.dateFrom);
    }
    if (filters.dateTo) {
      params = params.set('dateTo', filters.dateTo);
    }

    return params;
  }

  private activityLogParams(filters: ActivityLogFilters): HttpParams {
    let params = new HttpParams();

    if (filters.teamId !== null && filters.teamId !== undefined) {
      params = params.set('teamId', filters.teamId.toString());
    }
    if (filters.userId !== null && filters.userId !== undefined) {
      params = params.set('userId', filters.userId.toString());
    }
    if (filters.from) {
      params = params.set('from', filters.from);
    }
    if (filters.to) {
      params = params.set('to', filters.to);
    }

    return params;
  }
}
