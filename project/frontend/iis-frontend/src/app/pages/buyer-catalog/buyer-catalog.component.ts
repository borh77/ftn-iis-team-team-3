import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
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
  errorMessage = '';
  catalog: BuyerCatalog | null = null;

  ngOnInit(): void {
    this.loadCatalog();
  }

  loadCatalog(): void {
    this.loading = true;
    this.errorMessage = '';

    this.catalogService.getCatalog().subscribe({
      next: (catalog) => {
        this.catalog = catalog;
        this.loading = false;
      },
      error: () => {
        this.catalog = null;
        this.loading = false;
        this.errorMessage = 'Catalog could not be loaded.';
      },
    });
  }

  hasItems(): boolean {
    return !!this.catalog?.items?.length;
  }
}
