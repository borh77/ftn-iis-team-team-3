import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { SalesApiService } from '../../api/sales-api.service';
import { Customer } from '../../models/customer.model';
import { SalesProcess, SalesProcessRequest, SalesStage, } from '../../models/sales-process.model';
import { AuthService } from '../../../../core/auth/auth.service';
import { CreateCustomerNeedRequest } from '../../models/customer-need.model';
import { CreateOfferRequest } from '../../models/offer.model';
import { CreateContractRequest } from '../../models/contract.model';
import { Router } from '@angular/router';
import { extractBackendErrorMessage } from '../../../../core/http-error-message';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';

@Component({
  selector: 'app-processes-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './processes-list.component.html',
  styleUrls: ['./processes-list.component.css'],
})
export class ProcessesListComponent implements OnInit, OnDestroy {
  private readonly salesApiService = inject(SalesApiService);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);
  private readonly transientMessages = inject(TransientMessageService);

  processes: SalesProcess[] = [];
  customers: Customer[] = [];

  loading = true;
  saving = false;
  actionLoading = false;
  errorMessage = '';
  showCreateForm = false;

  canManageProcesses = false;
  canCreateProcess = false;
  canCreateNeedsAndOffers = false;

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
    this.canCreateNeedsAndOffers = this.authService.hasRole('ROLE_SALES_REPRESENTATIVE');
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.clearError();

    this.salesApiService.getSalesProcesses().pipe(finalize(() => (this.loading = false))).subscribe({
      next: (response) => {
        this.processes = response ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to load sales processes.'));
        this.cdr.detectChanges();
      },
    });

    this.salesApiService.getCustomers().subscribe({
      next: (response) => {
        this.customers = response ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to load customers.'));
      },
    });
  }

  createProcess(): void {
    this.saving = true;
    this.clearError();

    this.salesApiService.createSalesProcess(this.newProcess).pipe(finalize(() => (this.saving = false))).subscribe({
      next: () => {
        this.newProcess = {
          customerId: 0,
          title: '',
        };
        this.showCreateForm = false;
        this.loadData();
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to create sales process.'));
        this.cdr.detectChanges();
      },
    });
  }

  updateStage(process: SalesProcess, stage: SalesStage): void {
    this.clearError();
    this.salesApiService.updateSalesProcessStage(process.id, { stage }).subscribe({
      next: () => this.loadData(),
      error: (error) => this.showError(extractBackendErrorMessage(error, 'Failed to update stage.')),
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
    this.actionLoading = true;
    this.clearError();
    this.salesApiService.createCustomerNeed(process.customerId, this.newNeed).pipe(finalize(() => (this.actionLoading = false))).subscribe({
      next: () => {
        this.showNeedFormForProcessId = null;
        this.loadData();
      },
      error: (error) => this.showError(extractBackendErrorMessage(error, 'Failed to create customer need.')),
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
    this.actionLoading = true;
    this.clearError();
    this.salesApiService.createOffer(this.newOffer).pipe(finalize(() => (this.actionLoading = false))).subscribe({
      next: () => {
        this.showOfferFormForProcessId = null;
        this.loadData();
      },
      error: (error) => this.showError(extractBackendErrorMessage(error, 'Failed to create offer.')),
    });
  }

  viewDetails(process: SalesProcess): void {
    this.router.navigate(['/sales/processes', process.id]);
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private clearError(): void {
    this.transientMessages.clearField(this, 'errorMessage', () => this.cdr.detectChanges());
  }
}
