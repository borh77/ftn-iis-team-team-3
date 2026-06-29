import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { SalesApiService } from '../../api/sales-api.service';
import { Customer, CustomerRequest } from '../../models/customer.model';
import { AuthService } from '../../../../core/auth/auth.service';
import { Router } from '@angular/router';
import { extractBackendErrorMessage } from '../../../../core/http-error-message';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';

@Component({
  selector: 'app-customers-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './customers-list.component.html',
  styleUrls: ['./customers-list.component.css'],
})
export class CustomersListComponent implements OnInit, OnDestroy {
  private readonly salesApiService = inject(SalesApiService);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);
  private readonly transientMessages = inject(TransientMessageService);

  customers: Customer[] = [];
  loading = true;
  saving = false;
  errorMessage = '';
  showCreateForm = false;
  canManageCustomers = false;

  newCustomer: CustomerRequest = {
    name: '',
    email: '',
    phone: '',
    website: '',
    address: '',
  };

  ngOnInit(): void {
    this.canManageCustomers = this.authService.hasRole('ROLE_SALES_REPRESENTATIVE');
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.loading = true;
    this.clearError();

    this.salesApiService.getCustomers().pipe(finalize(() => (this.loading = false))).subscribe({
      next: (response) => {
        this.customers = response ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to load customers.'));
        this.cdr.detectChanges();
      },
    });
  }

  viewDetails(customer: Customer): void {
    this.router.navigate(['/sales/customers', customer.id]);
  }

  createCustomer(): void {
    this.saving = true;
    this.clearError();

    this.salesApiService.createCustomer(this.newCustomer).pipe(finalize(() => (this.saving = false))).subscribe({
      next: () => {
        this.newCustomer = {
          name: '',
          email: '',
          phone: '',
          website: '',
          address: '',
        };
        this.showCreateForm = false;
        this.loadCustomers();
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to create customer.'));
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
