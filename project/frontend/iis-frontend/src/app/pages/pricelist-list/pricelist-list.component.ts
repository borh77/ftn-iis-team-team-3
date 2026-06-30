import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { extractBackendErrorMessage } from '../../core/http-error-message';
import { PricelistService } from '../../core/pricelist.service';
import { Pricelist, PricelistCreationStep, PricelistItem, PricelistWizardState } from '../../core/pricelist.models';
import { PricelistWizardService } from '../../core/pricelist-wizard.service';
import { SpecialOfferService } from '../../core/special-offer.service';
import { DiscountType, PromotionSuggestion, SpecialOffer } from '../../core/special-offer.models';
import { ERROR_MESSAGE_MS, SUCCESS_MESSAGE_MS, TransientMessageService } from '../../core/transient-message.service';

type PricelistStatusFilter = Pricelist['status'] | 'ALL';

@Component({
  selector: 'app-pricelist-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pricelist-list.component.html',
  styleUrls: ['./pricelist-list.component.css'],
})
export class PricelistListComponent implements OnInit, OnDestroy {
  private readonly service = inject(PricelistService);
  private readonly wizardService = inject(PricelistWizardService);
  private readonly offerService = inject(SpecialOfferService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);
  private readonly transientMessages = inject(TransientMessageService);

  loading = false;
  loadingDrafts = false;
  changingStatusId: number | null = null;
  creatingVersionId: number | null = null;
  successMessage = '';
  errorMessage = '';
  draftsErrorMessage = '';
  toastErrorMessage = '';
  pricelists: Pricelist[] = [];
  wizardDrafts: PricelistWizardState[] = [];
  expandedOffers: Record<number, boolean> = {};
  offersByPricelist: Record<number, SpecialOffer[]> = {};
  offerForms: Record<number, OfferForm> = {};
  activationErrorByOfferId: Record<number, string> = {};
  loadingOffersId: number | null = null;
  changingOfferId: number | null = null;
  loadingSuggestionKeyByPricelist: Record<number, string> = {};
  promotionSuggestionsByPricelist: Record<number, PromotionSuggestion[]> = {};
  suggestionSegmentByPricelist: Record<number, string> = {};
  loadedSuggestionKeyByPricelist: Record<number, string> = {};
  suggestionErrorByPricelist: Record<number, string> = {};
  replacingItemId: number | null = null;
  selectedStatus: PricelistStatusFilter = 'ALL';

  readonly statusOptions: Array<{ value: PricelistStatusFilter; label: string }> = [
    { value: 'ALL', label: 'All statuses' },
    { value: 'DRAFT', label: 'Draft' },
    { value: 'IN_REVIEW', label: 'In review' },
    { value: 'ACTIVE', label: 'Active' },
    { value: 'ARCHIVED', label: 'Archived' },
  ];

  ngOnInit(): void {
    const navigationSuccess = globalThis.history?.state?.successMessage;
    if (navigationSuccess) {
      this.showSuccess(navigationSuccess);
    }
    this.loadDrafts();
    this.load();
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  load(): void {
    this.loading = true;
    this.clearError();
    this.service.team().subscribe({
      next: (list) => {
        this.loading = false;
        this.pricelists = [...list];
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.pricelists = [];
        this.showError('Team pricelists could not be loaded.');
        this.cdr.detectChanges();
      },
    });
  }

  reload(): void {
    this.loadDrafts();
    this.load();
  }

  get filteredPricelists(): Pricelist[] {
    if (this.selectedStatus === 'ALL') {
      return this.pricelists;
    }
    return this.pricelists.filter((pricelist) => pricelist.status === this.selectedStatus);
  }

  get statusFilterActive(): boolean {
    return this.selectedStatus !== 'ALL';
  }

  resetFilters(): void {
    this.selectedStatus = 'ALL';
  }

  loadDrafts(): void {
    this.loadingDrafts = true;
    this.clearDraftsError();
    this.wizardService.getDrafts().subscribe({
      next: (drafts) => {
        this.loadingDrafts = false;
        this.wizardDrafts = drafts;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loadingDrafts = false;
        this.wizardDrafts = [];
        this.showDraftsError('Unfinished drafts could not be loaded.');
        this.cdr.detectChanges();
      },
    });
  }

  continueDraft(draft: PricelistWizardState): void {
    this.router.navigate(['/pricelists/create', draft.pricelistId]);
  }

  createWizard(): void {
    this.router.navigate(['/pricelists/create']);
  }

  submitForReview(pricelist: Pricelist): void {
    this.changingStatusId = pricelist.id;
    this.clearResultMessages();
    this.wizardService.finishWizard(pricelist.id).pipe(finalize(() => (this.changingStatusId = null))).subscribe({
      next: () => {
        this.showSuccess('Pricelist was submitted for review.');
        this.loadDrafts();
        this.load();
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Pricelist could not be submitted for review.'));
        this.cdr.detectChanges();
      },
    });
  }

  editPricelist(pricelist: Pricelist): void {
    if (!this.canEdit(pricelist)) {
      this.showError('Only draft pricelists can be edited through the wizard.');
      return;
    }
    this.router.navigate(['/pricelists/create', pricelist.id]);
  }

  activate(pricelist: Pricelist): void {
    this.changeStatus(pricelist, 'ACTIVE');
  }

  returnToDraft(pricelist: Pricelist): void {
    const reason = window.prompt('Enter a rejection reason for returning this pricelist to draft:')?.trim();
    if (!reason) {
      this.showError('A rejection reason is required to return a pricelist to draft.');
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
    if (!item.replacementAvailable || !item.replacementVariantId) {
      this.showError('No replacement is defined for this inactive variant.');
      return;
    }
    this.replacingItemId = item.id;
    this.clearResultMessages();
    this.service.replaceVariant(pricelist.id, item.id).pipe(
      finalize(() => (this.replacingItemId = null))
    ).subscribe({
      next: () => {
        this.showSuccess('Variant was replaced.');
        this.load();
      },
      error: (error) => {
        this.showError(this.replaceVariantErrorMessage(error));
        this.cdr.detectChanges();
      },
    });
  }

  createNewVersion(pricelist: Pricelist): void {
    this.creatingVersionId = pricelist.id;
    this.clearResultMessages();

    this.service.createNewVersion(pricelist.id).pipe(finalize(() => (this.creatingVersionId = null))).subscribe({
      next: () => {
        this.showSuccess('New draft version was created.');
        this.load();
      },
      error: (error) => {
        this.showError(this.versionErrorMessage(error));
        this.cdr.detectChanges();
      },
    });
  }

  canSubmitForReview(pricelist: Pricelist): boolean {
    return pricelist.canSubmitForReview ?? (
      this.isOwner(pricelist)
      && pricelist.status === 'DRAFT'
      && (pricelist.creationStep === 'REVIEW' || pricelist.creationStep === 'COMPLETED')
    );
  }

  canEdit(pricelist: Pricelist): boolean {
    return pricelist.canEditDraft ?? (pricelist.status === 'DRAFT' && this.canCollaborate(pricelist));
  }

  canActivate(pricelist: Pricelist): boolean {
    return pricelist.status === 'IN_REVIEW'
      && pricelist.canActivate === true
      && !this.requiresReplacement(pricelist);
  }

  canReturnToDraft(pricelist: Pricelist): boolean {
    return pricelist.status === 'IN_REVIEW' && pricelist.canReject === true;
  }

  isWaitingForExternalReview(pricelist: Pricelist): boolean {
    return pricelist.status === 'IN_REVIEW'
      && this.isOwner(pricelist)
      && pricelist.canActivate !== true
      && pricelist.canReject !== true;
  }

  canArchive(pricelist: Pricelist): boolean {
    return this.isOwner(pricelist) && pricelist.status === 'ACTIVE';
  }

  canCreateNewVersion(pricelist: Pricelist): boolean {
    return pricelist.canCreateNewVersion === true;
  }

  canManageOffers(pricelist: Pricelist): boolean {
    return pricelist.canManageOffers === true;
  }

  canReplaceVariants(pricelist: Pricelist): boolean {
    return pricelist.status === 'DRAFT'
      && (pricelist.canEditDraft ?? this.canCollaborate(pricelist));
  }

  requiresReplacement(pricelist: Pricelist): boolean {
    return pricelist.items.some((item) => item.replacementRequired);
  }

  inactiveVariantLifecycleMessage(pricelist: Pricelist): string {
    if (pricelist.status === 'IN_REVIEW') {
      return 'This pricelist contains inactive catalog variants and cannot be activated. Return it to draft so the owner can replace inactive medicines.';
    }
    if (pricelist.status === 'ACTIVE') {
      return 'This active pricelist contains inactive catalog variants. Create a new version to replace inactive medicines.';
    }
    if (pricelist.status === 'ARCHIVED') {
      return 'This archived pricelist contains inactive catalog variants and is read-only.';
    }
    return 'This pricelist contains inactive catalog variants.';
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
      this.ensureSuggestionSegment(pricelist);
      this.ensurePromotionSuggestions(pricelist);
      this.loadOffers(pricelist.id);
    }
  }

  onSuggestionSegmentChanged(pricelist: Pricelist): void {
    this.ensurePromotionSuggestions(pricelist);
  }

  generatePromotionSuggestions(pricelist: Pricelist, force = true): void {
    const segment = this.ensureSuggestionSegment(pricelist).trim();
    if (!segment) {
      this.suggestionErrorByPricelist[pricelist.id] = 'Customer segment is required.';
      return;
    }

    const key = this.suggestionKey(pricelist.id, segment);
    if (!force && this.loadedSuggestionKeyByPricelist[pricelist.id] === key) {
      return;
    }
    if (this.loadingSuggestionKeyByPricelist[pricelist.id] === key) {
      return;
    }

    this.loadingSuggestionKeyByPricelist[pricelist.id] = key;
    this.suggestionErrorByPricelist[pricelist.id] = '';
    this.offerService.getPromotionSuggestions(segment).pipe(finalize(() => {
      if (this.loadingSuggestionKeyByPricelist[pricelist.id] === key) {
        delete this.loadingSuggestionKeyByPricelist[pricelist.id];
      }
    })).subscribe({
      next: (suggestions) => {
        if (this.suggestionKey(pricelist.id, this.ensureSuggestionSegment(pricelist)) !== key) {
          return;
        }
        this.promotionSuggestionsByPricelist[pricelist.id] = suggestions
          .filter((suggestion) => this.canApplySuggestionToPricelist(pricelist, suggestion))
          .slice(0, 5);
        this.loadedSuggestionKeyByPricelist[pricelist.id] = key;
        this.cdr.detectChanges();
      },
      error: (error) => {
        if (this.suggestionKey(pricelist.id, this.ensureSuggestionSegment(pricelist)) !== key) {
          return;
        }
        this.promotionSuggestionsByPricelist[pricelist.id] = [];
        delete this.loadedSuggestionKeyByPricelist[pricelist.id];
        this.suggestionErrorByPricelist[pricelist.id] = extractBackendErrorMessage(error, 'Promotion suggestions could not be loaded.');
        this.cdr.detectChanges();
      },
    });
  }

  applyPromotionSuggestion(pricelist: Pricelist, suggestion: PromotionSuggestion): void {
    const form = this.ensureOfferForm(pricelist);
    form.variantId = suggestion.variantId ?? form.variantId;
    form.discountType = suggestion.suggestedDiscountType;
    form.discountValue = suggestion.suggestedDiscountValue;
    this.showSuccess('Promotion suggestion was copied to the offer form. Review dates before saving.');
  }

  dismissPromotionSuggestion(pricelist: Pricelist, suggestion: PromotionSuggestion): void {
    this.promotionSuggestionsByPricelist[pricelist.id] = (this.promotionSuggestionsByPricelist[pricelist.id] ?? [])
      .filter((candidate) => candidate !== suggestion);
  }

  isLoadingSuggestions(pricelist: Pricelist): boolean {
    return !!this.loadingSuggestionKeyByPricelist[pricelist.id];
  }

  hasLoadedSuggestions(pricelist: Pricelist): boolean {
    return !!this.loadedSuggestionKeyByPricelist[pricelist.id];
  }

  segmentOptions(): string[] {
    return Array.from(new Set(
      this.pricelists
        .map((pricelist) => pricelist.customerSegment?.trim())
        .filter((segment): segment is string => !!segment)
    )).sort((first, second) => first.localeCompare(second));
  }

  targetType(suggestion: PromotionSuggestion): string {
    if (suggestion.brandId) {
      return 'Brand';
    }
    return 'Variant';
  }

  createOffer(pricelist: Pricelist): void {
    const form = this.ensureOfferForm(pricelist);
    this.clearResultMessages();

    if (!form.variantId || !form.discountValue || !form.startDate || !form.endDate) {
      this.showError('Offer could not be created.');
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
        this.showSuccess('Offer was created successfully.');
        this.offerForms[pricelist.id] = this.defaultOfferForm(pricelist);
        this.loadOffers(pricelist.id);
      },
      error: (error) => {
        this.showError(this.offerErrorMessage(error, 'create'));
      },
    });
  }

  activateOffer(offer: SpecialOffer): void {
    this.changingOfferId = offer.id;
    this.clearToastError();
    this.clearActivationError(offer.id);

    this.offerService.activate(offer.id).pipe(finalize(() => (this.changingOfferId = null))).subscribe({
      next: () => {
        this.clearToastError();
        this.clearActivationError(offer.id);
        this.showSuccess('Offer was activated successfully.');
        this.loadOffers(offer.pricelistId);
      },
      error: (err: HttpErrorResponse) => {
        const message = this.createErrorMessage(err);

        this.showToastError(message);
        this.showActivationError(offer.id, message);
        this.cdr.detectChanges();
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
    const labels: Record<Pricelist['status'], string> = {
      DRAFT: 'Draft',
      IN_REVIEW: 'In review',
      ACTIVE: 'Active',
      ARCHIVED: 'Archived',
    };
    return labels[status] ?? status.replace('_', ' ');
  }

  draftStepLabel(step: PricelistCreationStep): string {
    const labels: Record<PricelistCreationStep, string> = {
      BASIC_INFO: 'Basic information',
      TEAM_ACCESS: 'Team',
      ITEMS: 'Items',
      THRESHOLDS: 'Thresholds',
      REVIEW: 'Review',
      COMPLETED: 'Completed',
    };
    return labels[step] ?? step;
  }

  private showSuccess(message: string): void {
    this.transientMessages.setField(this, 'successMessage', message, SUCCESS_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private showDraftsError(message: string): void {
    this.transientMessages.setField(this, 'draftsErrorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private showToastError(message: string): void {
    this.transientMessages.setField(this, 'toastErrorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private showActivationError(offerId: number, message: string): void {
    this.transientMessages.set(
      this,
      `activationError:${offerId}`,
      (value) => {
        this.activationErrorByOfferId[offerId] = value;
      },
      () => this.activationErrorByOfferId[offerId] ?? '',
      message,
      ERROR_MESSAGE_MS,
      () => this.cdr.detectChanges()
    );
  }

  private clearResultMessages(): void {
    this.transientMessages.clearField(this, 'successMessage', () => this.cdr.detectChanges());
    this.clearError();
  }

  private clearError(): void {
    this.transientMessages.clearField(this, 'errorMessage', () => this.cdr.detectChanges());
  }

  private clearDraftsError(): void {
    this.transientMessages.clearField(this, 'draftsErrorMessage', () => this.cdr.detectChanges());
  }

  private clearToastError(): void {
    this.transientMessages.clearField(this, 'toastErrorMessage', () => this.cdr.detectChanges());
  }

  private clearActivationError(offerId: number): void {
    this.transientMessages.clear(
      this,
      `activationError:${offerId}`,
      () => {
        this.activationErrorByOfferId[offerId] = '';
      },
      () => this.cdr.detectChanges()
    );
  }

  private changeStatus(pricelist: Pricelist, targetStatus: Pricelist['status'], reason?: string): void {
    this.changingStatusId = pricelist.id;
    this.clearResultMessages();

    this.service.changeStatus(pricelist.id, { targetStatus, reason }).pipe(finalize(() => (this.changingStatusId = null))).subscribe({
      next: () => {
        this.showSuccess(this.statusChangeSuccessMessage(targetStatus));
        this.load();
      },
      error: (error) => {
        this.showError(this.statusChangeErrorMessage(error));
        this.cdr.detectChanges();
      },
    });
  }

  private statusChangeErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const backend = String(error.error?.error ?? '');
      if (backend.includes('You cannot activate a pricelist that you submitted for review.')) {
        return 'You cannot activate a pricelist that you submitted for review.';
      }
      if (backend.includes('another authorized reviewer')) {
        return 'A pricelist must be reviewed by another authorized user.';
      }
      if (backend.includes('Only the owner')) {
        return 'Only the owner can change this pricelist status.';
      }
      if (backend.includes('inactive catalog variants')) {
        return extractBackendErrorMessage(error, 'Pricelist contains inactive catalog variants. Replace them before continuing.');
      }
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
    return extractBackendErrorMessage(error, 'Pricelist status update failed.');
  }

  private statusChangeSuccessMessage(targetStatus: Pricelist['status']): string {
    const messages: Record<Pricelist['status'], string> = {
      DRAFT: 'Pricelist was returned for correction.',
      IN_REVIEW: 'Pricelist was submitted for review.',
      ACTIVE: 'Pricelist activated.',
      ARCHIVED: 'Pricelist archived.',
    };
    return messages[targetStatus];
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
    return extractBackendErrorMessage(error, 'New version could not be created.');
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
        this.showError('Offers could not be loaded.');
        this.cdr.detectChanges();
      },
    });
  }

  private replaceVariantErrorMessage(error: unknown): string {
    const backend = error instanceof HttpErrorResponse ? String(error.error?.error ?? '') : '';
    if (backend.includes('Only draft')) {
      return 'Only draft pricelists can replace withdrawn variants.';
    }
    if (backend.includes('No replacement is defined')) {
      return 'No replacement is defined for this inactive variant.';
    }
    if (backend.includes('catalog-defined replacement')) {
      return 'Selected variant is not the catalog-defined replacement.';
    }
    if (backend.includes('not active')) {
      return 'Catalog-defined replacement variant is not active.';
    }
    if (backend.includes('already exists')) {
      return 'Selected replacement variant already exists in this pricelist.';
    }
    if (backend.includes('access')) {
      return 'You do not have access to this pricelist.';
    }
    return extractBackendErrorMessage(error, 'Variant could not be replaced.');
  }

  private changeOfferStatus(offer: SpecialOffer, action: 'activate' | 'archive'): void {
    this.changingOfferId = offer.id;
    const request = action === 'activate' ? this.offerService.activate(offer.id) : this.offerService.archive(offer.id);
    request.pipe(finalize(() => (this.changingOfferId = null))).subscribe({
      next: () => {
        this.showSuccess(action === 'activate' ? 'Offer was activated successfully.' : 'Offer was archived successfully.');
        this.loadOffers(offer.pricelistId);
      },
      error: (error) => {
        this.showError(this.offerErrorMessage(error, action));
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
      return extractBackendErrorMessage(error, 'Offer could not be activated.');
    }
    return extractBackendErrorMessage(error, action === 'archive' ? 'Offer could not be archived.' : 'Offer could not be created.');
  }

  private createErrorMessage(error: HttpErrorResponse): string {
    const backendMessage = extractBackendErrorMessage(error, '');
    if (backendMessage) {
      return backendMessage;
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

  private ensureSuggestionSegment(pricelist: Pricelist): string {
    if (!this.suggestionSegmentByPricelist[pricelist.id]) {
      this.suggestionSegmentByPricelist[pricelist.id] = pricelist.customerSegment ?? '';
    }
    return this.suggestionSegmentByPricelist[pricelist.id];
  }

  private ensurePromotionSuggestions(pricelist: Pricelist): void {
    this.generatePromotionSuggestions(pricelist, false);
  }

  private suggestionKey(pricelistId: number, segment: string): string {
    return `${pricelistId}:${segment.trim().toLocaleLowerCase()}`;
  }

  private canApplySuggestionToPricelist(pricelist: Pricelist, suggestion: PromotionSuggestion): boolean {
    if (!suggestion.variantId) {
      return false;
    }
    return pricelist.items.some((item) => item.variantId === suggestion.variantId);
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
