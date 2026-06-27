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
}

export interface PerformanceReportFilters {
  teamId?: number | null;
  start: string;
  end: string;
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
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

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

    return this.http.get<SpringPage<PricelistActivityLog>>(`${this.apiBaseUrl}/api/admin/logs`, {
      params,
    });
  }

  getPerformanceReport(filters: PerformanceReportFilters): Observable<TeamPerformanceReport> {
    let params = new HttpParams().set('start', filters.start).set('end', filters.end);

    if (filters.teamId !== null && filters.teamId !== undefined) {
      params = params.set('teamId', filters.teamId.toString());
    }

    return this.http.get<TeamPerformanceReport>(
      `${this.apiBaseUrl}/api/admin/analytics/performance`,
      { params },
    );
  }
}
