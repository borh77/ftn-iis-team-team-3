import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProcurementOrder } from '../../core/procurement.models';
import { ProcurementService } from '../../core/procurement.service';
import { extractBackendErrorMessage } from '../../core/http-error-message';

@Component({
  selector: 'app-procurement-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './procurement-list.component.html',
  styleUrl: './procurement-list.component.css',
})
export class ProcurementListComponent implements OnInit {
  private readonly procurementService = inject(ProcurementService);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = false;
  errorMessage = '';
  orders: ProcurementOrder[] = [];
  expandedOrderIds = new Set<number>();

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.procurementService.listMyProcurements().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.orders = [];
        this.loading = false;
        this.errorMessage = extractBackendErrorMessage(error, 'Procurements could not be loaded.');
        this.cdr.detectChanges();
      },
    });
  }

  toggleOrder(order: ProcurementOrder): void {
    const expandedOrderIds = new Set(this.expandedOrderIds);
    if (expandedOrderIds.has(order.id)) {
      expandedOrderIds.delete(order.id);
    } else {
      expandedOrderIds.add(order.id);
    }
    this.expandedOrderIds = expandedOrderIds;
  }

  isExpanded(order: ProcurementOrder): boolean {
    return this.expandedOrderIds.has(order.id);
  }
}
