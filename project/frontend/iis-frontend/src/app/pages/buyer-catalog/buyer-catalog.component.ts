import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { finalize } from 'rxjs';
import { BuyerCatalogService } from '../../core/buyer-catalog.service';
import { BuyerCatalog } from '../../core/buyer-catalog.models';

@Component({
  selector: 'app-buyer-catalog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './buyer-catalog.component.html',
  styleUrl: './buyer-catalog.component.css',
})
export class BuyerCatalogComponent implements OnInit {
  private readonly catalogService = inject(BuyerCatalogService);

  loading = false;
  refreshing = false;
  errorMessage = '';
  catalog: BuyerCatalog | null = null;

  ngOnInit(): void {
    this.loadCatalog();
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
    this.errorMessage = '';

    this.catalogService.getCatalog()
      .pipe(finalize(() => {
        this.loading = false;
        this.refreshing = false;
      }))
      .subscribe({
        next: (catalog) => {
          this.catalog = catalog;
        },
        error: (err: HttpErrorResponse) => {
          this.catalog = null;
          this.errorMessage = this.createErrorMessage(err);
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
}
