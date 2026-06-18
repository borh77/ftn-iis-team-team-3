import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PricelistService } from '../../core/pricelist.service';
import { Pricelist, PricelistItem } from '../../core/pricelist.models';
import { SpecialOfferService } from '../../core/special-offer.service';
import { DiscountType, SpecialOffer } from '../../core/special-offer.models';
import { CatalogService } from '../../core/catalog.service';
import { CatalogVariant } from '../../core/catalog.model';

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
  private readonly catalogService = inject(CatalogService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);

  loading = false;
  changingStatusId: number | null = null;
  creatingVersionId: number | null = null;
  successMessage = '';
  errorMessage = '';
  toastErrorMessage = '';
  pricelists: Pricelist[] = [];
  expandedOffers: Record<number, boolean> = {};
  offersByPricelist: Record<number, SpecialOffer[]> = {};
  offerForms: Record<number, OfferForm> = {};
  activationErrorByOfferId: Record<number, string> = {};
  loadingOffersId: number | null = null;
  changingOfferId: number | null = null;
  replacingItemId: number | null = null;
  replacementVariantIds: Record<number, number | null> = {};
  activeVariants: CatalogVariant[] = [];

  ngOnInit(): void {
    this.loadActiveVariants();
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.service.team().subscribe({
      next: (list) => {
        this.loading = false;
        this.pricelists = [...list];
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.pricelists = [];
        this.errorMessage = 'Team pricelists could not be loaded.';
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

  editPricelist(pricelist: Pricelist): void {
    this.router.navigate(['/pricelists', pricelist.id, 'edit']);
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

  replaceVariant(pricelist: Pricelist, item: PricelistItem): void {
    if (!item.id) {
      return;
    }
    const replacementVariantId = this.replacementVariantIds[item.id];
    if (!replacementVariantId) {
      this.errorMessage = 'Variant could not be replaced.';
      return;
    }
    this.replacingItemId = item.id;
    this.successMessage = '';
    this.errorMessage = '';
    this.service.replaceVariant(pricelist.id, item.id, Number(replacementVariantId)).subscribe({
      next: () => {
        this.replacingItemId = null;
        this.successMessage = 'Variant was replaced.';
        this.load();
      },
      error: (error) => {
        this.replacingItemId = null;
        this.errorMessage = this.replaceVariantErrorMessage(error);
        this.cdr.detectChanges();
      },
    });
  }

  createNewVersion(pricelist: Pricelist): void {
    this.creatingVersionId = pricelist.id;
    this.successMessage = '';
    this.errorMessage = '';

    this.service.createNewVersion(pricelist.id).subscribe({
      next: () => {
        this.creatingVersionId = null;
        this.successMessage = 'New draft version was created.';
        this.load();
      },
      error: (error) => {
        this.creatingVersionId = null;
        this.errorMessage = this.versionErrorMessage(error);
        this.cdr.detectChanges();
      },
    });
  }

  canSubmitForReview(pricelist: Pricelist): boolean {
    return this.isOwner(pricelist) && pricelist.status === 'DRAFT';
  }

  canEdit(pricelist: Pricelist): boolean {
    return pricelist.status === 'DRAFT' && this.canCollaborate(pricelist);
  }

  canActivate(pricelist: Pricelist): boolean {
    return this.isOwner(pricelist) && pricelist.status === 'IN_REVIEW';
  }

  canReturnToDraft(pricelist: Pricelist): boolean {
    return this.isOwner(pricelist) && pricelist.status === 'IN_REVIEW';
  }

  canArchive(pricelist: Pricelist): boolean {
    return this.isOwner(pricelist) && pricelist.status === 'ACTIVE';
  }

  canCreateNewVersion(pricelist: Pricelist): boolean {
    return pricelist.canCreateNewVersion || ((pricelist.status === 'IN_REVIEW' || pricelist.status === 'ACTIVE') && this.canCollaborate(pricelist));
  }

  canManageOffers(pricelist: Pricelist): boolean {
    return pricelist.canManageOffers ?? this.canCollaborate(pricelist);
  }

  canReplaceVariants(pricelist: Pricelist): boolean {
    return pricelist.status === 'DRAFT' && this.canCollaborate(pricelist);
  }

  requiresReplacement(pricelist: Pricelist): boolean {
    return pricelist.items.some((item) => item.replacementRequired);
  }

  isOwner(pricelist: Pricelist): boolean {
    return pricelist.owner === true;
  }

  isTeamPricelist(pricelist: Pricelist): boolean {
    return !this.isOwner(pricelist) && this.canCollaborate(pricelist);
  }

  private canCollaborate(pricelist: Pricelist): boolean {
    return pricelist.canCollaborate === true;
  }

  isChanging(pricelist: Pricelist): boolean {
    return this.changingStatusId === pricelist.id;
  }

  isCreatingVersion(pricelist: Pricelist): boolean {
    return this.creatingVersionId === pricelist.id;
  }

  isReplacing(item: PricelistItem): boolean {
    return item.id != null && this.replacingItemId === item.id;
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
    this.changingOfferId = offer.id;
    this.toastErrorMessage = '';
    this.activationErrorByOfferId[offer.id] = '';

    this.offerService.activate(offer.id).subscribe({
      next: () => {
        this.changingOfferId = null;
        this.toastErrorMessage = '';
        this.activationErrorByOfferId[offer.id] = '';
        this.successMessage = 'Offer was activated successfully.';
        this.loadOffers(offer.pricelistId);
      },
      error: (err: HttpErrorResponse) => {
        const message = this.createErrorMessage(err);

        this.changingOfferId = null;
        this.toastErrorMessage = message;
        this.activationErrorByOfferId[offer.id] = message;
        this.cdr.detectChanges();

        setTimeout(() => {
          if (this.toastErrorMessage === message) {
            this.toastErrorMessage = '';
            this.cdr.detectChanges();
          }
        }, 6000);
      },
    });
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
        const backend = String(error.error?.error ?? '');
        if (backend.includes('Only the owner')) {
          return 'Only the owner can change this pricelist status.';
        }
        if (backend.includes('inactive catalog variants')) {
          return 'Pricelist contains inactive catalog variants. Replace them before continuing.';
        }
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

  private versionErrorMessage(error: unknown): string {
    const backend = error instanceof HttpErrorResponse ? String(error.error?.error ?? '') : '';
    if (backend.includes('Archived pricelists')) {
      return 'Archived pricelists cannot be versioned.';
    }
    if (backend.includes('Draft pricelists')) {
      return 'Draft pricelists can be edited directly.';
    }
    if (backend.includes('access')) {
      return 'You do not have access to this pricelist.';
    }
    return 'New version could not be created.';
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

  private loadActiveVariants(): void {
    this.catalogService.listVariants().subscribe({
      next: (variants) => {
        this.activeVariants = variants;
        this.cdr.detectChanges();
      },
      error: () => {
        this.activeVariants = [];
      },
    });
  }

  private replaceVariantErrorMessage(error: unknown): string {
    const backend = error instanceof HttpErrorResponse ? String(error.error?.error ?? '') : '';
    if (backend.includes('Only draft')) {
      return 'Only draft pricelists can replace withdrawn variants.';
    }
    if (backend.includes('not active')) {
      return 'Selected replacement variant is not active.';
    }
    if (backend.includes('already exists')) {
      return 'Selected replacement variant already exists in this pricelist.';
    }
    if (backend.includes('access')) {
      return 'You do not have access to this pricelist.';
    }
    return 'Variant could not be replaced.';
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
    if (backend.includes('access')) {
      return 'You do not have access to this pricelist.';
    }
    if (action === 'activate') {
      return 'Offer could not be activated.';
    }
    return 'Offer could not be created.';
  }

  private createErrorMessage(error: HttpErrorResponse): string {
    if (typeof error.error?.error === 'string' && error.error.error.trim()) {
      return error.error.error.trim();
    }

    if (typeof error.error?.message === 'string' && error.error.message.trim()) {
      return error.error.message.trim();
    }

    if (typeof error.error?.detail === 'string' && error.error.detail.trim()) {
      return error.error.detail.trim();
    }

    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error.trim();
    }

    if (error.status === 400 || error.status === 422) {
      return 'Offer activation failed because the promotion validation rules were not satisfied.';
    }

    if (error.status === 403) {
      return 'You are not allowed to activate this offer.';
    }

    return 'Offer activation failed.';
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
