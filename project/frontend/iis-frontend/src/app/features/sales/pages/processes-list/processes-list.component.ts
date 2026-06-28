import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Customer } from '../../models/customer.model';
import { SalesProcess, SalesProcessRequest, SalesStage, } from '../../models/sales-process.model';
import { AuthService } from '../../../../core/auth/auth.service';
import { CreateCustomerNeedRequest } from '../../models/customer-need.model';
import { CreateOfferRequest } from '../../models/offer.model';
import { CreateContractRequest } from '../../models/contract.model';
import { Router } from '@angular/router';
import { SalesApiService, SalesMarketProduct } from '../../api/sales-api.service';

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
  private readonly router = inject(Router);

  processes: SalesProcess[] = [];
  availableTransitionsByProcessId: Record<number, SalesStage[]> = {};
  customers: Customer[] = [];
  offerMarketProducts: SalesMarketProduct[] = [];
  selectedOfferVariantId = 0;
  offerPriceMessage = '';

  loading = true;
  saving = false;
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

    this.salesApiService.getSalesProcesses().subscribe({
      next: (response) => {
        this.processes = response ?? [];
        this.loadAvailableTransitions();
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

  loadAvailableTransitions(): void {
    this.availableTransitionsByProcessId = {};

    this.processes.forEach((process) => {
      this.salesApiService.getAvailableStageTransitions(process.id).subscribe({
        next: (stages) => {
          this.availableTransitionsByProcessId[process.id] = stages ?? [];
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error(`Failed to load available transitions for process ${process.id}:`, error);
          this.availableTransitionsByProcessId[process.id] = [];
          this.cdr.detectChanges();
        },
      });
    });
  }

  getAvailableStages(process: SalesProcess): SalesStage[] {
    return this.availableTransitionsByProcessId[process.id] ?? [];
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

  viewDetails(process: SalesProcess): void {
    this.router.navigate(['/sales/processes', process.id]);
  }

  getCustomerRegionId(process: SalesProcess): number | null {
    const customer = this.customers.find((item) => item.id === process.customerId);
    return customer?.regionId ?? null;
  }

  openOfferForm(process: SalesProcess): void {
    this.showOfferFormForProcessId = process.id;
    this.offerMarketProducts = [];
    this.selectedOfferVariantId = 0;
    this.offerPriceMessage = '';

    this.newOffer = {
      customerId: process.customerId,
      salesProcessId: process.id,
      validUntil: '',
      notes: '',
      items: [
        {
          productId: 0,
          quantity: 1,
          unitPrice: 0,
        },
      ],
    };

    const regionId = this.getCustomerRegionId(process);

    if (!regionId) {
      this.offerPriceMessage = 'Customer does not have a region selected.';
      return;
    }

    this.salesApiService.getMarketProductsByRegion(regionId).subscribe({
      next: (response) => {
        this.offerMarketProducts = response ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load market products:', error);
        this.offerPriceMessage = 'Failed to load products for customer region.';
      },
    });
  }

  onOfferVariantChanged(process: SalesProcess, variantId: number): void {
    this.selectedOfferVariantId = Number(variantId);

    const selected = this.offerMarketProducts.find((item) => item.variantId === this.selectedOfferVariantId);

    if (!selected) {
      return;
    }

    this.newOffer.items[0].productId = selected.productId;
    this.refreshOfferPrice(process);
  }

  onOfferQuantityChanged(process: SalesProcess, quantity: number): void {
    this.newOffer.items[0].quantity = Number(quantity);
    this.refreshOfferPrice(process);
  }

  refreshOfferPrice(process: SalesProcess): void {
    const regionId = this.getCustomerRegionId(process);

    if (!regionId || !this.selectedOfferVariantId || this.newOffer.items[0].quantity <= 0) {
      return;
    }

    this.salesApiService
      .getSalesPrice(regionId, this.selectedOfferVariantId, this.newOffer.items[0].quantity)
      .subscribe({
        next: (response) => {
          this.newOffer.items[0].unitPrice = response.unitPrice;
          this.offerPriceMessage = `Price loaded from pricelist: ${response.unitPrice} ${response.currency}`;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Failed to load price:', error);
          this.offerPriceMessage = 'No active price found for selected product and quantity.';
          this.newOffer.items[0].unitPrice = 0;
          this.cdr.detectChanges();
        },
      });
  }
}