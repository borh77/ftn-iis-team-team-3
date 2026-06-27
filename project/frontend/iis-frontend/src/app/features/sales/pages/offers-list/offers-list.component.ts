import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { SalesApiService } from '../../api/sales-api.service';
import { Offer, UpdateOfferRequest } from '../../models/offer.model';
import { CreateContractRequest } from '../../models/contract.model';
import { Contract } from '../../models/contract.model';

@Component({
  selector: 'app-offers-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './offers-list.component.html',
  styleUrls: ['./offers-list.component.css'],
})
export class OffersListComponent implements OnInit {
  private readonly salesApiService = inject(SalesApiService);
  private readonly cdr = inject(ChangeDetectorRef);

  offers: Offer[] = [];
  contracts: Contract[] = [];
  loading = true;

  showContractFormForOfferId: number | null = null;

  saving = false;
  editingOfferId: number | null = null;

  editOffer: UpdateOfferRequest = {
    validUntil: '',
    notes: '',
  };

  startEditOffer(offer: Offer): void {
    this.editingOfferId = offer.id;
    this.editOffer = {
      validUntil: offer.validUntil,
      notes: offer.notes ?? '',
    };
  }

  cancelEditOffer(): void {
    this.editingOfferId = null;
  }

  updateOffer(offer: Offer): void {
    this.saving = true;

    this.salesApiService.updateOffer(offer.id, this.editOffer).subscribe({
      next: () => {
        this.editingOfferId = null;
        this.saving = false;
        this.loadData();
      },
      error: (error) => {
        console.error('Failed to update offer:', error);
        this.saving = false;
        this.cdr.detectChanges();
      },
    });
  }

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

    this.salesApiService.getOffers().subscribe({
      next: (response) => {
        this.offers = response ?? [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load offers:', error);
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  loadData(): void {
    this.loading = true;

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
            console.error('Failed to load contracts:', error);
            this.loading = false;
            this.cdr.detectChanges();
            },
        });
        },
        error: (error) => {
        console.error('Failed to load offers:', error);
        this.loading = false;
        this.cdr.detectChanges();
        },
    });
  }

  contractExistsForOffer(offerId: number): boolean {
    return this.contracts.some((contract) => contract.offerId === offerId);
  }

  acceptOffer(offer: Offer): void {
    this.salesApiService.acceptOffer(offer.id).subscribe({
      next: () => this.loadData(),
      error: (error) => console.error('Failed to accept offer:', error),
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
    this.salesApiService.createContract(this.newContract).subscribe({
      next: () => {
        this.showContractFormForOfferId = null;
        this.loadData();
      },
      error: (error) => console.error('Failed to create contract:', error),
    });
  }
}