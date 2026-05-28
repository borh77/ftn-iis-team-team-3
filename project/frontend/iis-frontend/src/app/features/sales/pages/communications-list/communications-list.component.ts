import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { SalesApiService } from '../../api/sales-api.service';
import { Customer } from '../../models/customer.model';
import { CommunicationType, CustomerCommunication, CustomerCommunicationRequest } from '../../models/customer-communication.model';
import { AuthService } from '../../../../core/auth/auth.service';

@Component({
  selector: 'app-communications-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './communications-list.component.html',
  styleUrls: ['./communications-list.component.css'],
})
export class CommunicationsListComponent implements OnInit {
  private readonly salesApiService = inject(SalesApiService);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  customers: Customer[] = [];
  communications: CustomerCommunication[] = [];

  loading = false;
  saving = false;
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
        console.error('Failed to load customers:', error);
      },
    });
  }

  loadCommunications(): void {
    if (!this.selectedCustomerId) {
      this.communications = [];
      return;
    }

    this.loading = true;

    this.salesApiService
      .getCustomerCommunications(this.selectedCustomerId)
      .subscribe({
        next: (response) => {
          this.communications = response ?? [];
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Failed to load communications:', error);
          this.loading = false;
          this.cdr.detectChanges();
        },
      });
  }

  createCommunication(): void {
    if (!this.selectedCustomerId) {
      return;
    }

    this.saving = true;

    this.salesApiService
      .createCustomerCommunication(
        this.selectedCustomerId,
        this.newCommunication,
      )
      .subscribe({
        next: () => {
          this.newCommunication = {
            type: 'EMAIL',
            communicationDate: '',
            summary: '',
          };

          this.saving = false;

          this.loadCommunications();
        },
        error: (error) => {
          console.error('Failed to create communication:', error);
          this.saving = false;
          this.cdr.detectChanges();
        },
      });
  }
}