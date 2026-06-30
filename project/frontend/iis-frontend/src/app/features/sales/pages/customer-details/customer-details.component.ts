import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, finalize, forkJoin, of } from 'rxjs';

import { SalesApiService } from '../../api/sales-api.service';
import { Customer } from '../../models/customer.model';
import { SalesProcess } from '../../models/sales-process.model';
import { CustomerCommunication } from '../../models/customer-communication.model';
import { CustomerNeed } from '../../models/customer-need.model';
import { Offer } from '../../models/offer.model';
import { Contract } from '../../models/contract.model';
import { extractBackendErrorMessage } from '../../../../core/http-error-message';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';

@Component({
  selector: 'app-customer-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './customer-details.component.html',
  styleUrl: './customer-details.component.css',
})
export class CustomerDetailsComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly salesApiService = inject(SalesApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly transientMessages = inject(TransientMessageService);

  customer?: Customer;
  processes: SalesProcess[] = [];
  communications: CustomerCommunication[] = [];
  needs: CustomerNeed[] = [];
  offers: Offer[] = [];
  contracts: Contract[] = [];

  loading = true;
  errorMessage = '';

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    forkJoin({
      customers: this.salesApiService.getCustomers(),
      processes: this.salesApiService.getSalesProcesses().pipe(catchError(() => of([]))),
      communications: this.salesApiService.getCustomerCommunications(id).pipe(catchError(() => of([]))),
      needs: this.salesApiService.getCustomerNeeds(id).pipe(catchError(() => of([]))),
      offers: this.salesApiService.getOffers().pipe(catchError(() => of([]))),
      contracts: this.salesApiService.getContracts().pipe(catchError(() => of([]))),
    }).pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }),
    ).subscribe({
      next: (data) => {
        this.customer = data.customers.find((customer) => customer.id === id);

        this.processes = data.processes.filter((process) => process.customerId === id);
        this.communications = data.communications;
        this.needs = data.needs;
        this.offers = data.offers.filter((offer) => offer.customerId === id);
        this.contracts = data.contracts.filter((contract) => contract.customerId === id);

        this.cdr.detectChanges();
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to load customer details.'));
        this.cdr.detectChanges();
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/sales/customers']);
  }

  viewProcess(process: SalesProcess): void {
    this.router.navigate(['/sales/processes', process.id]);
  }
  startSalesProcess(): void {
    this.router.navigate(['/sales/processes'], {
      queryParams: { customerId: this.customer?.id },
    });
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }
}
