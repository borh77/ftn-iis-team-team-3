import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AdverseEffectsApiService } from '../../api/adverse-effects-api.service';
import { AdverseEffectReport } from '../../models/adverse-effect-report.model';
import { ERROR_MESSAGE_MS, SUCCESS_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';

@Component({
  selector: 'app-my-reports',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './my-reports.component.html',
  styleUrls: ['./my-reports.component.css']
})
export class MyReportsComponent implements OnInit, OnDestroy {

  private readonly api = inject(AdverseEffectsApiService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly transientMessages = inject(TransientMessageService);

  reports: AdverseEffectReport[] = [];
  loading = true;
  errorMessage = '';
  successMessage = '';

  ngOnInit(): void {
    const nav = this.router.getCurrentNavigation();
    const state = nav?.extras?.state ?? history.state;
    if (state?.['successMessage']) {
      this.showSuccess(state['successMessage']);
    }

    this.api.getMyReports().subscribe({
      next: (data) => {
        this.reports = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.showError('Error loading reports.');
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  // Edit is only allowed when status is SUBMITTED
  canEdit(report: AdverseEffectReport): boolean {
    return report.status === 'SUBMITTED';
  }

  goToCreate(): void {
    this.router.navigate(['/adverse-effects/create-doctor-report']);
  }

  goToEdit(id: number): void {
    this.router.navigate(['/adverse-effects/edit-report', id]);
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

  private showSuccess(message: string): void {
    this.transientMessages.setField(this, 'successMessage', message, SUCCESS_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }
}
