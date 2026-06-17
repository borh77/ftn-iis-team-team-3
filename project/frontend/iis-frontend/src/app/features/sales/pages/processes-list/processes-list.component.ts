import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { SalesApiService } from '../../api/sales-api.service';
import { Customer } from '../../models/customer.model';
import { SalesProcess, SalesProcessRequest, SalesStage, } from '../../models/sales-process.model';
import { AuthService } from '../../../../core/auth/auth.service';
import { CreateCustomerNeedRequest } from '../../models/customer-need.model';
import { CreateOfferRequest } from '../../models/offer.model';
import { CreateContractRequest } from '../../models/contract.model';

@Component({
  selector: 'app-processes-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './processes-list.component.html',
  styleUrls: ['./processes-list.component.css'],
})
export class ProcessesListComponent implements OnInit {
  private readonly salesApiService = inject(SalesApiService);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  processes: SalesProcess[] = [];
  customers: Customer[] = [];

  loading = true;
  saving = false;
  showCreateForm = false;

  canManageProcesses = false;
  canCreateProcess = false;

  showNeedFormForProcessId: number | null = null;
  showOfferFormForProcessId: number | null = null;
  showContractFormForProcessId: number | null = null;

  newNeed: CreateCustomerNeedRequest = {
    salesProcessId: 0,
    description: '',
    priority: 'HIGH',
  };

  newOffer: CreateOfferRequest = {
    customerId: 0,
    salesProcessId: 0,
    validUntil: '',
    notes: '',
    items: [
      {
        productId: 1,
        quantity: 1,
        unitPrice: 1000,
      },
    ],
  };

  newContract: CreateContractRequest = {
    offerId: 0,
    startDate: '',
    endDate: '',
    terms: '',
  };

  stages: SalesStage[] = [
    'NEW',
    'CONTACTED',
    'QUALIFIED',
    'PROPOSAL_SENT',
    'NEGOTIATION',
    'WON',
    'LOST',
  ];

  newProcess: SalesProcessRequest = {
    customerId: 0,
    title: '',
  };

  ngOnInit(): void {
    this.canManageProcesses =
        this.authService.hasRole('ROLE_SALES_REPRESENTATIVE') ||
        this.authService.hasRole('ROLE_SALES_MANAGER');
    this.canCreateProcess = this.authService.hasRole('ROLE_SALES_REPRESENTATIVE');
    this.loadData();
  }

  loadData(): void {
    this.loading = true;

    this.salesApiService.getSalesProcesses().subscribe({
      next: (response) => {
        this.processes = response ?? [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load sales processes:', error);
        this.loading = false;
        this.cdr.detectChanges();
      },
    });

    this.salesApiService.getCustomers().subscribe({
      next: (response) => {
        this.customers = response ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load customers:', error);
      },
    });
  }

  createProcess(): void {
    this.saving = true;

    this.salesApiService.createSalesProcess(this.newProcess).subscribe({
      next: () => {
        this.newProcess = {
          customerId: 0,
          title: '',
        };
        this.showCreateForm = false;
        this.saving = false;
        this.loadData();
      },
      error: (error) => {
        console.error('Failed to create sales process:', error);
        this.saving = false;
        this.cdr.detectChanges();
      },
    });
  }

  updateStage(process: SalesProcess, stage: SalesStage): void {
    this.salesApiService.updateSalesProcessStage(process.id, { stage }).subscribe({
      next: () => this.loadData(),
      error: (error) => console.error('Failed to update stage:', error),
    });
  }

  openNeedForm(process: SalesProcess): void {
    this.showNeedFormForProcessId = process.id;
    this.newNeed = {
      salesProcessId: process.id,
      description: '',
      priority: 'HIGH',
    };
  }

  createNeed(process: SalesProcess): void {
    this.salesApiService.createCustomerNeed(process.customerId, this.newNeed).subscribe({
      next: () => {
        this.showNeedFormForProcessId = null;
        this.loadData();
      },
      error: (error) => console.error('Failed to create customer need:', error),
    });
  }

  openOfferForm(process: SalesProcess): void {
    this.showOfferFormForProcessId = process.id;
    this.newOffer = {
      customerId: process.customerId,
      salesProcessId: process.id,
      validUntil: '',
      notes: '',
      items: [
        {
          productId: 1,
          quantity: 1,
          unitPrice: 1000,
        },
      ],
    };
  }

  createOffer(): void {
    this.salesApiService.createOffer(this.newOffer).subscribe({
      next: () => {
        this.showOfferFormForProcessId = null;
        this.loadData();
      },
      error: (error) => console.error('Failed to create offer:', error),
    });
  }
}