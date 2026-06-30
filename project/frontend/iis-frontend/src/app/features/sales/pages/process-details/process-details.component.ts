import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin, of } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/auth/auth.service';

import { SalesApiService, SalesMarketProduct } from '../../api/sales-api.service';
import { Customer } from '../../models/customer.model';
import { SalesProcess, SalesStage } from '../../models/sales-process.model';
import { CreateCustomerNeedRequest, CustomerNeed } from '../../models/customer-need.model';
import { CustomerCommunication } from '../../models/customer-communication.model';
import { CreateOfferRequest, Offer } from '../../models/offer.model';
import { Contract } from '../../models/contract.model';
import { SalesProcessHistory } from '../../models/sales-process-history.model';
import { CustomerCommunicationRequest } from '../../models/customer-communication.model';
import { SalesStageDefinition } from '../../models/sales-workflow.model';
import { extractBackendErrorMessage } from '../../../../core/http-error-message';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';

@Component({
  selector: 'app-process-details',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './process-details.component.html',
  styleUrl: './process-details.component.css',
})
export class ProcessDetailsComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly salesApiService = inject(SalesApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  private readonly transientMessages = inject(TransientMessageService);

  process?: SalesProcess;
  customer?: Customer;
  needs: CustomerNeed[] = [];
  communications: CustomerCommunication[] = [];
  offers: Offer[] = [];
  contracts: Contract[] = [];
  history: SalesProcessHistory[] = [];
  availableTransitions: SalesStage[] = [];
  workflowStageDefinitions: SalesStageDefinition[] = [];
  workflowStages: string[] = [];
  offerMarketProducts: SalesMarketProduct[] = [];
  selectedStage: SalesStage = '';

  loading = true;
  savingStage = false;
  savingCommunication = false;
  errorMessage = '';
  showCommunicationForm = false;
  showNeedForm = false;
  showOfferForm = false;
  selectedOfferVariantId = 0;
  offerPriceMessage = '';
  canManageCommunications = false;
  canManageProcesses = false;
  canCreateNeedsAndOffers = false;

  stages: string[] = [];

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
        productId: 0,
        quantity: 1,
        unitPrice: 0,
      },
    ],
  };

  newCommunication: CustomerCommunicationRequest = {
    type: 'MEETING',
    communicationDate: '',
    summary: '',
  };

  ngOnInit(): void {
    this.canManageCommunications =
     this.authService.hasRole('ROLE_ACCOUNT_MANAGER');
    this.canManageProcesses = this.authService.hasRole('ROLE_SALES_REPRESENTATIVE');
    this.canCreateNeedsAndOffers = this.authService.hasRole('ROLE_SALES_REPRESENTATIVE');

    this.loadProcessDetails();
  }

  loadProcessDetails(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loading = true;

    this.salesApiService.getSalesProcessById(id).pipe(finalize(() => (this.loading = false))).subscribe({
      next: (process) => {
        this.process = process;
        this.selectedStage = process.stage;
        this.resetNeedForm(process);
        this.resetOfferForm(process);
        this.collapseFormsForClosedProcess();

        forkJoin({
          customers: this.salesApiService.getCustomers(),
          needs: this.salesApiService.getCustomerNeeds(process.customerId),
          communications: this.salesApiService.getCustomerCommunications(process.customerId),
          offers: this.salesApiService.getOffers(),
          contracts: this.salesApiService.getContracts(),
          history: this.salesApiService.getSalesProcessHistory(process.id),
          availableTransitions: process.workflowId
            ? this.salesApiService.getAvailableStageTransitions(process.id)
            : of([]),
          workflowStages: process.workflowId
            ? this.salesApiService.getSalesWorkflowStages(process.workflowId)
            : of([]),
        }).subscribe({
          next: (data) => {
            this.customer = data.customers.find((customer) => customer.id === process.customerId);
            this.needs = data.needs.filter((need) => need.salesProcessId === process.id);
            this.communications = data.communications;
            this.offers = data.offers.filter((offer) => offer.salesProcessId === process.id);
            this.contracts = data.contracts.filter((contract) => contract.salesProcessId === process.id);
            this.history = data.history;
            this.availableTransitions = data.availableTransitions ?? [];
            this.workflowStageDefinitions = [...(data.workflowStages ?? [])]
              .sort((first, second) => first.stageOrder - second.stageOrder);
            this.workflowStages = this.workflowStageDefinitions.map((stage) => stage.name);
            this.stages = this.workflowStages.includes(process.stage)
              ? this.workflowStages
              : [process.stage, ...this.workflowStages];

            this.cdr.detectChanges();
          },
          error: (error) => {
            this.showError(extractBackendErrorMessage(error, 'Failed to load process details.'));
            this.cdr.detectChanges();
          },
        });
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to load process.'));
        this.cdr.detectChanges();
      },
    });
  }

  updateStage(stage: SalesStage): void {
    if (!this.process || stage === this.process.stage || this.isStageDropdownDisabled()) {
      this.selectedStage = this.process?.stage ?? '';
      return;
    }

    if (this.isClosingStage(stage) && !window.confirm('Are you sure you want to close this sales process?')) {
      this.selectedStage = this.process.stage;
      this.cdr.detectChanges();
      return;
    }

    this.savingStage = true;

    this.salesApiService.updateSalesProcessStage(this.process.id, { stage }).subscribe({
      next: () => {
        this.savingStage = false;
        this.loadProcessDetails();
      },
      error: (error) => {
        console.error('Failed to update stage:', error);
        this.savingStage = false;
        this.selectedStage = this.process?.stage ?? '';
        this.cdr.detectChanges();
      },
    });
  }

  isStageDropdownDisabled(): boolean {
    return !this.canManageProcesses ||
      this.isProcessClosed() ||
      !this.process?.workflowId ||
      this.availableTransitions.length === 0 ||
      this.savingStage;
  }

  getStageDisabledMessage(): string {
    if (this.isProcessClosed()) {
      return 'Process is closed.';
    }

    if (!this.process?.workflowId) {
      return 'This process has no workflow, so stage changes are disabled.';
    }

    if (!this.canManageProcesses) {
      return 'You do not have permission to change the process stage.';
    }

    if (this.availableTransitions.length === 0) {
      return 'No workflow transitions are available from the current stage.';
    }

    return '';
  }

  isProcessClosed(): boolean {
    return this.process?.status === 'SUCCESSFUL' || this.process?.status === 'UNSUCCESSFUL';
  }

  isClosingStage(stageName: SalesStage): boolean {
    return this.workflowStageDefinitions.some(
      (stage) => stage.name === stageName && stage.endStage,
    );
  }

  collapseFormsForClosedProcess(): void {
    if (!this.isProcessClosed() || !this.process) {
      return;
    }

    this.showNeedForm = false;
    this.showOfferForm = false;
    this.resetNeedForm(this.process);
    this.resetOfferForm(this.process);
  }

  resetNeedForm(process: SalesProcess): void {
    this.newNeed = {
      salesProcessId: process.id,
      description: '',
      priority: 'HIGH',
    };
  }

  createNeed(): void {
    if (!this.process) {
      return;
    }

    this.salesApiService.createCustomerNeed(this.process.customerId, this.newNeed).subscribe({
      next: () => {
        this.closeNeedForm();
        this.loadProcessDetails();
      },
      error: (error) => console.error('Failed to create customer need:', error),
    });
  }

  closeNeedForm(): void {
    if (this.process) {
      this.resetNeedForm(this.process);
    }

    this.showNeedForm = false;
  }

  resetOfferForm(process: SalesProcess): void {
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
    this.selectedOfferVariantId = 0;
    this.offerPriceMessage = '';
    this.offerMarketProducts = [];
  }

  openOfferForm(): void {
    if (!this.process) {
      return;
    }

    this.showOfferForm = true;
    this.offerMarketProducts = [];
    this.resetOfferForm(this.process);

    const regionId = this.customer?.regionId ?? null;

    if (!regionId) {
      this.offerPriceMessage = 'Customer does not have a region selected.';
      return;
    }

    this.salesApiService.getMarketProductsByRegion(regionId).subscribe({
      next: (response) => {
        this.offerMarketProducts = response ?? [];
        this.offerPriceMessage = this.offerMarketProducts.length === 0
          ? 'No market products available for this customer region.'
          : '';
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load market products:', error);
        this.offerPriceMessage = 'Failed to load products for customer region.';
        this.cdr.detectChanges();
      },
    });
  }

  createOffer(): void {
    if (!this.isOfferFormSubmittable()) {
      return;
    }

    this.salesApiService.createOffer(this.newOffer).subscribe({
      next: () => {
        this.closeOfferForm();
        this.loadProcessDetails();
      },
      error: (error) => console.error('Failed to create offer:', error),
    });
  }

  closeOfferForm(): void {
    if (this.process) {
      this.resetOfferForm(this.process);
    }

    this.showOfferForm = false;
  }

  onOfferVariantChanged(variantId: number): void {
    this.selectedOfferVariantId = Number(variantId);

    const selected = this.offerMarketProducts.find((item) => item.variantId === this.selectedOfferVariantId);

    if (!selected) {
      return;
    }

    this.newOffer.items[0].productId = selected.productId;
    this.refreshOfferPrice();
  }

  onOfferQuantityChanged(quantity: number): void {
    this.newOffer.items[0].quantity = Number(quantity);
    this.refreshOfferPrice();
  }

  refreshOfferPrice(): void {
    const regionId = this.customer?.regionId ?? null;

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

  isOfferFormSubmittable(): boolean {
    return !!this.customer?.regionId &&
      this.offerMarketProducts.length > 0 &&
      this.newOffer.items[0].productId !== 0 &&
      this.newOffer.items[0].unitPrice > 0;
  }

  addCommunication(): void {
    if (!this.canManageCommunications || !this.process) {
        return;
    }

    this.savingCommunication = true;
    this.clearError();
    this.salesApiService
        .createCustomerCommunication(this.process.customerId, this.newCommunication)
        .pipe(finalize(() => (this.savingCommunication = false)))
        .subscribe({
        next: () => {
            this.showCommunicationForm = false;
            this.newCommunication = {
            type: 'MEETING',
            communicationDate: '',
            summary: '',
            };

            this.loadProcessDetails();
        },
        error: (error) => this.showError(extractBackendErrorMessage(error, 'Failed to add communication.')),
        });
    }

  goBack(): void {
    this.router.navigate(['/sales/processes']);
  }

  isStageReached(stage: string): boolean {
    if (!this.process) {
      return false;
    }

    return this.stages.indexOf(stage) <= this.stages.indexOf(this.process.stage);
  }

  formatDate(value?: string | null): string {
    if (!value) {
      return '-';
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return new Intl.DateTimeFormat('en-GB', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(date);
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
