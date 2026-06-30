import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { SalesApiService } from '../../api/sales-api.service';
import { Offer } from '../../models/offer.model';
import { CreateContractRequest } from '../../models/contract.model';
import { Contract } from '../../models/contract.model';
import { extractBackendErrorMessage } from '../../../../core/http-error-message';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';

@Component({
  selector: 'app-offers-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './offers-list.component.html',
  styleUrls: ['./offers-list.component.css'],
})
export class OffersListComponent implements OnInit, OnDestroy {
  private readonly salesApiService = inject(SalesApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly transientMessages = inject(TransientMessageService);

  offers: Offer[] = [];
  contracts: Contract[] = [];
  loading = true;
  savingContract = false;
  acceptingOfferId: number | null = null;
  errorMessage = '';

  showContractFormForOfferId: number | null = null;

  newContract: CreateContractRequest = {
    offerId: 0,
    startDate: '',
    endDate: '',
    terms: '',
  };

  ngOnInit(): void {
    this.loadData();
  }

  loadOffers(): void {
    this.loading = true;
    this.clearError();

    this.salesApiService.getOffers().pipe(finalize(() => (this.loading = false))).subscribe({
      next: (response) => {
        this.offers = response ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to load offers.'));
        this.cdr.detectChanges();
      },
    });
  }

  loadData(): void {
    this.loading = true;
    this.clearError();

    this.salesApiService.getOffers().subscribe({
        next: (offers) => {
        this.offers = offers ?? [];
        this.salesApiService.getContracts().subscribe({
            next: (contracts) => {
            this.contracts = contracts ?? [];
            this.loading = false;
            this.cdr.detectChanges();
            },
            error: (error) => {
            this.showError(extractBackendErrorMessage(error, 'Failed to load contracts.'));
            this.loading = false;
            this.cdr.detectChanges();
            },
        });
        },
        error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to load offers.'));
        this.loading = false;
        this.cdr.detectChanges();
        },
    });
  }

  contractExistsForOffer(offerId: number): boolean {
    return this.contracts.some((contract) => contract.offerId === offerId);
  }

  acceptOffer(offer: Offer): void {
    this.acceptingOfferId = offer.id;
    this.clearError();
    this.salesApiService.acceptOffer(offer.id).pipe(finalize(() => (this.acceptingOfferId = null))).subscribe({
      next: () => this.loadData(),
      error: (error) => this.showError(extractBackendErrorMessage(error, 'Failed to accept offer.')),
    });
  }

  openContractForm(offer: Offer): void {
    this.showContractFormForOfferId = offer.id;
    this.newContract = {
      offerId: offer.id,
      startDate: '',
      endDate: '',
      terms: '',
    };
  }

  canAcceptOffer(offer: Offer): boolean {
   return offer.status !== 'ACCEPTED' && offer.salesProcessTitle !== '';
  }

  createContract(): void {
    this.savingContract = true;
    this.clearError();
    this.salesApiService.createContract(this.newContract).pipe(finalize(() => (this.savingContract = false))).subscribe({
      next: () => {
        this.showContractFormForOfferId = null;
        this.loadData();
      },
      error: (error) => this.showError(extractBackendErrorMessage(error, 'Failed to create contract.')),
    });
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
