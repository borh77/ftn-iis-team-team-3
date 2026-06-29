import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/auth/auth.service';

import { SalesApiService } from '../../api/sales-api.service';
import { SalesProcess } from '../../models/sales-process.model';
import { CustomerNeed } from '../../models/customer-need.model';
import { CustomerCommunication } from '../../models/customer-communication.model';
import { Offer } from '../../models/offer.model';
import { Contract } from '../../models/contract.model';
import { SalesProcessHistory } from '../../models/sales-process-history.model';
import { CustomerCommunicationRequest } from '../../models/customer-communication.model';
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
  needs: CustomerNeed[] = [];
  communications: CustomerCommunication[] = [];
  offers: Offer[] = [];
  contracts: Contract[] = [];
  history: SalesProcessHistory[] = [];

  loading = true;
  savingCommunication = false;
  errorMessage = '';
  showCommunicationForm = false;
  canManageCommunications = false;

  stages = ['NEW', 'CONTACTED', 'QUALIFIED', 'PROPOSAL_SENT', 'NEGOTIATION', 'WON', 'LOST'];

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.canManageCommunications =
     this.authService.hasRole('ROLE_ACCOUNT_MANAGER');

    this.salesApiService.getSalesProcessById(id).pipe(finalize(() => (this.loading = false))).subscribe({
      next: (process) => {
        this.process = process;

        forkJoin({
          needs: this.salesApiService.getCustomerNeeds(process.customerId),
          communications: this.salesApiService.getCustomerCommunications(process.customerId),
          offers: this.salesApiService.getOffers(),
          contracts: this.salesApiService.getContracts(),
          history: this.salesApiService.getSalesProcessHistory(process.id),
        }).subscribe({
          next: (data) => {
            this.needs = data.needs.filter((need) => need.salesProcessId === process.id);
            this.communications = data.communications;
            this.offers = data.offers.filter((offer) => offer.salesProcessId === process.id);
            this.contracts = data.contracts.filter((contract) => contract.salesProcessId === process.id);
            this.history = data.history;

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

  newCommunication: CustomerCommunicationRequest = {
    type: 'MEETING',
    communicationDate: '',
    summary: '',
  };

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

            this.ngOnInit();
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
