import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AdverseEffectsApiService } from '../../api/adverse-effects-api.service';
import {
  AdverseEffectReport,
  AnalystNote,
  ChangeStatusRequest,
  ReportStatus,
  StatusTransition
} from '../../models/adverse-effect-report.model';
import { AuthService } from '../../../../core/auth/auth.service';

@Component({
  selector: 'app-report-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  templateUrl: './report-detail.component.html',
  styleUrls: ['./report-detail.component.css']
})
export class ReportDetailComponent implements OnInit {

  private readonly api = inject(AdverseEffectsApiService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  report: AdverseEffectReport | null = null;
  statusHistory: StatusTransition[] = [];
  notes: AnalystNote[] = [];
  reportId: number | null = null;
  loading = true;
  actionLoading = false;
  errorMessage = '';
  successMessage = '';

  selectedTransition: ReportStatus | null = null;
  statusForm: ChangeStatusRequest = { newStatus: 'UNDER_REVIEW' };
  newNoteContent = '';

  get isPharmacovigilant(): boolean {
    return this.auth.hasRole('ROLE_FARMAKOVIGILANT');
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = Number(idParam);

    if (!idParam || isNaN(id)) {
      this.errorMessage = 'Invalid report ID.';
      this.loading = false;
      this.cdr.detectChanges();
      return;
    }

    this.reportId = id;
    this.loadReport(id);
    this.loadStatusHistory(id);
    this.loadNotes(id);
  }

  startTransition(newStatus: ReportStatus): void {
    this.selectedTransition = newStatus;
    this.successMessage = '';
    this.errorMessage = '';
    this.statusForm = {
      newStatus,
      comment: '',
      priority: newStatus === 'UNDER_REVIEW' ? 'MEDIUM' : undefined,
      closureReason: '',
      verdict: ''
    };
  }

  cancelTransition(): void {
    this.selectedTransition = null;
    this.statusForm = { newStatus: 'UNDER_REVIEW' };
  }

  confirmStatusChange(): void {
    if (!this.reportId || !this.selectedTransition) return;

    if (this.selectedTransition === 'CLOSED' && !this.statusForm.comment?.trim()) {
      this.errorMessage = 'Comment is required when closing a report.';
      this.cdr.detectChanges();
      return;
    }

    this.actionLoading = true;
    this.errorMessage = '';

    this.api.changeStatus(this.reportId, this.statusForm).subscribe({
      next: (updated) => {
        this.report = updated;
        this.successMessage = 'Status changed successfully.';
        this.selectedTransition = null;
        this.actionLoading = false;
        this.loadStatusHistory(this.reportId!);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || `Error changing status (${err.status}).`;
        this.actionLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  addNote(): void {
    if (!this.reportId || !this.newNoteContent.trim()) return;

    this.actionLoading = true;
    this.errorMessage = '';

    this.api.addNote(this.reportId, { content: this.newNoteContent.trim() }).subscribe({
      next: (note) => {
        this.notes = [...this.notes, note];
        this.newNoteContent = '';
        this.successMessage = 'Note added successfully.';
        this.actionLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || `Error adding note (${err.status}).`;
        this.actionLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  getAvailableTransitions(): ReportStatus[] {
    if (!this.isPharmacovigilant || !this.report) return [];
    if (this.report.reportType === 'PATIENT') return [];
    if (this.report.status === 'SUBMITTED') return ['UNDER_REVIEW'];
    if (this.report.status === 'UNDER_REVIEW') return ['CLOSED'];
    return [];
  }

  getTransitionLabel(status: ReportStatus): string {
    const labels: Record<ReportStatus, string> = {
      SUBMITTED: 'Move to Submitted',
      UNDER_REVIEW: 'Move to Under Review',
      CLOSED: 'Mark as Closed',
      EVIDENCED: 'Mark as Evidenced'
    };
    return labels[status];
  }

  getFinalStatusMessage(): string {
    if (this.report?.reportType === 'PATIENT') {
      return 'Patient reports are evidenced automatically and are not analyzed.';
    }

    return 'This report is in a final status.';
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

  goBack(): void {
    this.router.navigate([this.isPharmacovigilant ? '/adverse-effects/all-reports' : '/adverse-effects/my-reports']);
  }

  private loadReport(id: number): void {
    this.api.getReportById(id).subscribe({
      next: (data) => {
        this.report = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = `Error loading report (${err.status}).`;
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private loadStatusHistory(id: number): void {
    this.api.getStatusHistory(id).subscribe({
      next: (data) => {
        this.statusHistory = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.statusHistory = [];
        this.cdr.detectChanges();
      }
    });
  }

  private loadNotes(id: number): void {
    this.api.getNotes(id).subscribe({
      next: (data) => {
        this.notes = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.notes = [];
        this.cdr.detectChanges();
      }
    });
  }
}
