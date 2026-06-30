import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { SalesApiService } from '../../api/sales-api.service';
import { Customer } from '../../models/customer.model';
import { CommunicationType, CustomerCommunication, CustomerCommunicationRequest } from '../../models/customer-communication.model';
import { AuthService } from '../../../../core/auth/auth.service';
import { extractBackendErrorMessage } from '../../../../core/http-error-message';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';

@Component({
  selector: 'app-communications-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './communications-list.component.html',
  styleUrls: ['./communications-list.component.css'],
})
export class CommunicationsListComponent implements OnInit, OnDestroy {
  private readonly salesApiService = inject(SalesApiService);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly transientMessages = inject(TransientMessageService);

  customers: Customer[] = [];
  communications: CustomerCommunication[] = [];

  loading = false;
  saving = false;
  errorMessage = '';
  canCreateCommunication = false;

  selectedCustomerId = 0;

  communicationTypes: CommunicationType[] = [
    'EMAIL',
    'PHONE_CALL',
    'MEETING',
  ];

  newCommunication: CustomerCommunicationRequest = {
    type: 'EMAIL',
    communicationDate: '',
    summary: '',
  };

  ngOnInit(): void {
    this.canCreateCommunication =
      this.authService.hasRole('ROLE_ACCOUNT_MANAGER');

    this.loadCustomers();
  }

  loadCustomers(): void {
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

  loadCommunications(): void {
    if (!this.selectedCustomerId) {
      this.communications = [];
      return;
    }

    this.loading = true;
    this.clearError();

    this.salesApiService
      .getCustomerCommunications(this.selectedCustomerId)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (response) => {
          this.communications = response ?? [];
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.showError(extractBackendErrorMessage(error, 'Failed to load communications.'));
          this.cdr.detectChanges();
        },
      });
  }

  createCommunication(): void {
    if (!this.selectedCustomerId) {
      return;
    }

    this.saving = true;
    this.clearError();

    this.salesApiService
      .createCustomerCommunication(
        this.selectedCustomerId,
        this.newCommunication,
      )
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.newCommunication = {
            type: 'EMAIL',
            communicationDate: '',
            summary: '',
          };

          this.loadCommunications();
        },
        error: (error) => {
          this.showError(extractBackendErrorMessage(error, 'Failed to create communication.'));
          this.cdr.detectChanges();
        },
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
