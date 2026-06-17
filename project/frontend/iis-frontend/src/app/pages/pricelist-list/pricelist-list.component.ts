import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PricelistService } from '../../core/pricelist.service';
import { Pricelist } from '../../core/pricelist.models';
import { SpecialOfferService } from '../../core/special-offer.service';
import { DiscountType, SpecialOffer } from '../../core/special-offer.models';

@Component({
  selector: 'app-pricelist-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pricelist-list.component.html',
  styleUrls: ['./pricelist-list.component.css'],
})
export class PricelistListComponent implements OnInit {
  private readonly service = inject(PricelistService);
  private readonly offerService = inject(SpecialOfferService);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = false;
  changingStatusId: number | null = null;
  successMessage = '';
  errorMessage = '';
  pricelists: Pricelist[] = [];
  expandedOffers: Record<number, boolean> = {};
  offersByPricelist: Record<number, SpecialOffer[]> = {};
  offerForms: Record<number, OfferForm> = {};
  loadingOffersId: number | null = null;
  changingOfferId: number | null = null;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.service.mine().subscribe({
      next: (list) => {
        this.loading = false;
        this.pricelists = [...list];
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.pricelists = [];
        this.cdr.detectChanges();
      },
    });
  }

  reload(): void {
    this.load();
  }

  submitForReview(pricelist: Pricelist): void {
    this.changeStatus(pricelist, 'IN_REVIEW');
  }

  activate(pricelist: Pricelist): void {
    this.changeStatus(pricelist, 'ACTIVE');
  }

  returnToDraft(pricelist: Pricelist): void {
    const reason = window.prompt('Enter a reason for returning this pricelist to draft:')?.trim();
    if (!reason) {
      this.errorMessage = 'A reason is required to return a pricelist to draft.';
      return;
    }
    this.changeStatus(pricelist, 'DRAFT', reason);
  }

  archive(pricelist: Pricelist): void {
    this.changeStatus(pricelist, 'ARCHIVED');
  }

  canSubmitForReview(pricelist: Pricelist): boolean {
    return pricelist.status === 'DRAFT';
  }

  canActivate(pricelist: Pricelist): boolean {
    return pricelist.status === 'IN_REVIEW';
  }

  canReturnToDraft(pricelist: Pricelist): boolean {
    return pricelist.status === 'IN_REVIEW';
  }

  canArchive(pricelist: Pricelist): boolean {
    return pricelist.status === 'ACTIVE';
  }

  isChanging(pricelist: Pricelist): boolean {
    return this.changingStatusId === pricelist.id;
  }

  toggleOffers(pricelist: Pricelist): void {
    this.expandedOffers[pricelist.id] = !this.expandedOffers[pricelist.id];
    if (this.expandedOffers[pricelist.id]) {
      this.ensureOfferForm(pricelist);
      this.loadOffers(pricelist.id);
    }
  }

  createOffer(pricelist: Pricelist): void {
    const form = this.ensureOfferForm(pricelist);
    this.successMessage = '';
    this.errorMessage = '';

    if (!form.variantId || !form.discountValue || !form.startDate || !form.endDate) {
      this.errorMessage = 'Offer could not be created.';
      return;
    }

    this.offerService.create({
      pricelistId: pricelist.id,
      variantId: Number(form.variantId),
      discountType: form.discountType,
      discountValue: Number(form.discountValue),
      startDate: new Date(form.startDate).toISOString(),
      endDate: new Date(form.endDate).toISOString(),
    }).subscribe({
      next: () => {
        this.successMessage = 'Offer was created successfully.';
        this.offerForms[pricelist.id] = this.defaultOfferForm(pricelist);
        this.loadOffers(pricelist.id);
      },
      error: (error) => {
        this.errorMessage = this.offerErrorMessage(error, 'create');
      },
    });
  }

  activateOffer(offer: SpecialOffer): void {
    this.changeOfferStatus(offer, 'activate');
  }

  archiveOffer(offer: SpecialOffer): void {
    this.changeOfferStatus(offer, 'archive');
  }

  isOfferChanging(offer: SpecialOffer): boolean {
    return this.changingOfferId === offer.id;
  }

  statusLabel(status: Pricelist['status']): string {
    return status.replace('_', ' ');
  }

  private changeStatus(pricelist: Pricelist, targetStatus: Pricelist['status'], reason?: string): void {
    this.changingStatusId = pricelist.id;
    this.successMessage = '';
    this.errorMessage = '';

    this.service.changeStatus(pricelist.id, { targetStatus, reason }).subscribe({
      next: () => {
        this.changingStatusId = null;
        this.successMessage = 'Pricelist status was updated successfully.';
        this.load();
      },
      error: (error) => {
        this.changingStatusId = null;
        this.errorMessage = this.statusChangeErrorMessage(error);
        this.cdr.detectChanges();
      },
    });
  }

  private statusChangeErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 400) {
        return 'This status change is not allowed.';
      }
      if (error.status === 409) {
        return 'A conflict exists with an already existing pricelist.';
      }
      if (error.status === 404) {
        return 'Pricelist was not found.';
      }
    }
    return 'Pricelist status update failed.';
  }

  private loadOffers(pricelistId: number): void {
    this.loadingOffersId = pricelistId;
    this.offerService.listForPricelist(pricelistId).subscribe({
      next: (offers) => {
        this.offersByPricelist[pricelistId] = offers;
        this.loadingOffersId = null;
        this.cdr.detectChanges();
      },
      error: () => {
        this.offersByPricelist[pricelistId] = [];
        this.loadingOffersId = null;
        this.errorMessage = 'Offers could not be loaded.';
        this.cdr.detectChanges();
      },
    });
  }

  private changeOfferStatus(offer: SpecialOffer, action: 'activate' | 'archive'): void {
    this.changingOfferId = offer.id;
    const request = action === 'activate' ? this.offerService.activate(offer.id) : this.offerService.archive(offer.id);
    request.subscribe({
      next: () => {
        this.changingOfferId = null;
        this.successMessage = action === 'activate' ? 'Offer was activated successfully.' : 'Offer was archived successfully.';
        this.loadOffers(offer.pricelistId);
      },
      error: (error) => {
        this.changingOfferId = null;
        this.errorMessage = this.offerErrorMessage(error, action);
      },
    });
  }

  private offerErrorMessage(error: unknown, action: 'create' | 'activate' | 'archive'): string {
    const backend = error instanceof HttpErrorResponse ? String(error.error?.error ?? '') : '';
    if (backend.includes('Discount value')) {
      return 'Discount value is invalid.';
    }
    if (backend.includes('period')) {
      return 'Offer period must be inside the pricelist period.';
    }
    if (backend.includes('variant')) {
      return 'Selected variant is not part of this pricelist.';
    }
    if (backend.includes('reduce price below zero')) {
      return 'Discount cannot reduce price below zero.';
    }
    if (backend.includes('Base price')) {
      return 'Base price could not be determined.';
    }
    if (action === 'activate') {
      return 'Offer could not be activated.';
    }
    return 'Offer could not be created.';
  }

  private ensureOfferForm(pricelist: Pricelist): OfferForm {
    if (!this.offerForms[pricelist.id]) {
      this.offerForms[pricelist.id] = this.defaultOfferForm(pricelist);
    }
    return this.offerForms[pricelist.id];
  }

  private defaultOfferForm(pricelist: Pricelist): OfferForm {
    return {
      variantId: pricelist.items[0]?.variantId ?? null,
      discountType: 'PERCENTAGE',
      discountValue: null,
      startDate: '',
      endDate: '',
    };
  }
}

interface OfferForm {
  variantId: number | null;
  discountType: DiscountType;
  discountValue: number | null;
  startDate: string;
  endDate: string;
}
