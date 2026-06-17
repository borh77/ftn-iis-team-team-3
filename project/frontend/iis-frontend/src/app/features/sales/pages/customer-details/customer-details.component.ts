import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { SalesApiService } from '../../api/sales-api.service';
import { Customer } from '../../models/customer.model';
import { SalesProcess } from '../../models/sales-process.model';
import { CustomerCommunication } from '../../models/customer-communication.model';
import { CustomerNeed } from '../../models/customer-need.model';
import { Offer } from '../../models/offer.model';
import { Contract } from '../../models/contract.model';

@Component({
  selector: 'app-customer-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './customer-details.component.html',
  styleUrl: './customer-details.component.css',
})
export class CustomerDetailsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly salesApiService = inject(SalesApiService);
  private readonly cdr = inject(ChangeDetectorRef);

  customer?: Customer;
  processes: SalesProcess[] = [];
  communications: CustomerCommunication[] = [];
  needs: CustomerNeed[] = [];
  offers: Offer[] = [];
  contracts: Contract[] = [];

  loading = true;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    forkJoin({
      customers: this.salesApiService.getCustomers(),
      processes: this.salesApiService.getSalesProcesses(),
      communications: this.salesApiService.getCustomerCommunications(id),
      needs: this.salesApiService.getCustomerNeeds(id),
      offers: this.salesApiService.getOffers(),
      contracts: this.salesApiService.getContracts(),
    }).subscribe({
      next: (data) => {
        this.customer = data.customers.find((customer) => customer.id === id);

        this.processes = data.processes.filter((process) => process.customerId === id);
        this.communications = data.communications;
        this.needs = data.needs;
        this.offers = data.offers.filter((offer) => offer.customerId === id);
        this.contracts = data.contracts.filter((contract) => contract.customerId === id);

        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load customer details:', error);
        this.loading = false;
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
}