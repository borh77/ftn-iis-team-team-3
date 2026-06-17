import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { PricelistService } from '../../core/pricelist.service';
import { Pricelist } from '../../core/pricelist.models';

@Component({
  selector: 'app-pricelist-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pricelist-list.component.html',
  styleUrls: ['./pricelist-list.component.css'],
})
export class PricelistListComponent implements OnInit {
  private readonly service = inject(PricelistService);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = false;
  changingStatusId: number | null = null;
  successMessage = '';
  errorMessage = '';
  pricelists: Pricelist[] = [];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.service.mine().subscribe({
      next: (list) => {
        this.loading = false;
        this.pricelists = [...list];
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.pricelists = [];
        this.cdr.detectChanges();
      },
    });
  }

  reload(): void {
    this.load();
  }

  submitForReview(pricelist: Pricelist): void {
    this.changeStatus(pricelist, 'IN_REVIEW');
  }

  activate(pricelist: Pricelist): void {
    this.changeStatus(pricelist, 'ACTIVE');
  }

  returnToDraft(pricelist: Pricelist): void {
    const reason = window.prompt('Enter a reason for returning this pricelist to draft:')?.trim();
    if (!reason) {
      this.errorMessage = 'A reason is required to return a pricelist to draft.';
      return;
    }
    this.changeStatus(pricelist, 'DRAFT', reason);
  }

  archive(pricelist: Pricelist): void {
    this.changeStatus(pricelist, 'ARCHIVED');
  }

  canSubmitForReview(pricelist: Pricelist): boolean {
    return pricelist.status === 'DRAFT';
  }

  canActivate(pricelist: Pricelist): boolean {
    return pricelist.status === 'IN_REVIEW';
  }

  canReturnToDraft(pricelist: Pricelist): boolean {
    return pricelist.status === 'IN_REVIEW';
  }

  canArchive(pricelist: Pricelist): boolean {
    return pricelist.status === 'ACTIVE';
  }

  isChanging(pricelist: Pricelist): boolean {
    return this.changingStatusId === pricelist.id;
  }

  statusLabel(status: Pricelist['status']): string {
    return status.replace('_', ' ');
  }

  private changeStatus(pricelist: Pricelist, targetStatus: Pricelist['status'], reason?: string): void {
    this.changingStatusId = pricelist.id;
    this.successMessage = '';
    this.errorMessage = '';

    this.service.changeStatus(pricelist.id, { targetStatus, reason }).subscribe({
      next: () => {
        this.changingStatusId = null;
        this.successMessage = 'Pricelist status was updated successfully.';
        this.load();
      },
      error: (error) => {
        this.changingStatusId = null;
        this.errorMessage = this.statusChangeErrorMessage(error);
        this.cdr.detectChanges();
      },
    });
  }

  private statusChangeErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 400) {
        return 'This status change is not allowed.';
      }
      if (error.status === 409) {
        return 'A conflict exists with an already existing pricelist.';
      }
      if (error.status === 404) {
        return 'Pricelist was not found.';
      }
    }
    return 'Pricelist status update failed.';
  }
}
