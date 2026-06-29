import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { finalize, take } from 'rxjs';
import { BuyerCatalogService } from '../../core/buyer-catalog.service';
import { BuyerCatalog } from '../../core/buyer-catalog.models';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../core/transient-message.service';

@Component({
  selector: 'app-buyer-catalog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './buyer-catalog.component.html',
  styleUrl: './buyer-catalog.component.css',
})
export class BuyerCatalogComponent implements OnInit, OnDestroy {
  private readonly catalogService = inject(BuyerCatalogService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly transientMessages = inject(TransientMessageService);

  loading = false;
  refreshing = false;
  errorMessage = '';
  catalog: BuyerCatalog | null = null;

  ngOnInit(): void {
    this.loadCatalog();
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  loadCatalog(): void {
    this.requestCatalog(true);
  }

  refreshCatalog(): void {
    this.requestCatalog(false);
  }

  private requestCatalog(initialLoad: boolean): void {
    this.loading = initialLoad;
    this.refreshing = !initialLoad;
    this.clearError();

    this.catalogService.getCatalog()
      .pipe(
        take(1),
        finalize(() => {
          this.loading = false;
          this.refreshing = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (catalog) => {
          this.catalog = catalog;
          this.clearError();
          this.cdr.detectChanges();
        },
        error: (err: HttpErrorResponse) => {
          this.catalog = null;
          this.showError(this.createErrorMessage(err));
          this.cdr.detectChanges();
        },
      });
  }

  private createErrorMessage(error: HttpErrorResponse): string {
    if (typeof error.error?.error === 'string' && error.error.error.trim()) {
      return error.error.error.trim();
    }

    if (typeof error.error?.message === 'string' && error.error.message.trim()) {
      return error.error.message.trim();
    }

    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error.trim();
    }

    if (error.status === 403) {
      return 'You are not allowed to view the buyer catalog.';
    }

    if (error.status === 404) {
      return 'No active catalog is available for your region and customer segment.';
    }

    return 'Catalog could not be loaded.';
  }

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private clearError(): void {
    this.transientMessages.clearField(this, 'errorMessage', () => this.cdr.detectChanges());
  }
}
