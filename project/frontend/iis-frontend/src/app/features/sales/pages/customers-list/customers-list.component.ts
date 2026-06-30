import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { RegionService } from '../../../../core/region.service';
import { Region } from '../../../../core/region.model';
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
  private readonly regionService = inject(RegionService);
  private readonly transientMessages = inject(TransientMessageService);

  customers: Customer[] = [];
  regions: Region[] = [];
  loading = true;
  saving = false;
  errorMessage = '';
  showCreateForm = false;
  canManageCustomers = false;
  customerSearchTerm = '';
  customerStatusFilter = '';

  editingCustomerId: number | null = null;

  newCustomer: CustomerRequest = {
    name: '',
    email: '',
    phone: '',
    website: '',
    address: '',
    regionId: null,
  };

  editCustomer: CustomerRequest = {
    name: '',
    email: '',
    phone: '',
    website: '',
    address: '',
    regionId: null,
  };

  ngOnInit(): void {
    this.canManageCustomers = this.authService.hasRole('ROLE_SALES_REPRESENTATIVE');
    this.loadCustomers();
    this.loadRegions();
  }

  loadCustomers(): void {
    this.loading = true;
    this.clearError();

    this.salesApiService.getCustomers().pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }),
    ).subscribe({
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

  loadRegions(): void {
    this.regionService.list().subscribe({
      next: (response) => {
        this.regions = response ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load regions:', error);
      },
    });
  }

  viewDetails(customer: Customer): void {
    this.router.navigate(['/sales/customers', customer.id]);
  }

  createCustomer(): void {
    this.saving = true;
    this.clearError();

    this.salesApiService.createCustomer(this.newCustomer).pipe(
      finalize(() => {
        this.saving = false;
        this.cdr.detectChanges();
      }),
    ).subscribe({
      next: () => {
        this.newCustomer = {
          name: '',
          email: '',
          phone: '',
          website: '',
          address: '',
          regionId: null,
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

  startEditCustomer(customer: Customer): void {
    this.editingCustomerId = customer.id;
    this.editCustomer = {
      name: customer.name,
      email: customer.email,
      phone: customer.phone ?? '',
      website: customer.website ?? '',
      address: customer.address ?? '',
      regionId: customer.regionId,
    };
  }

  cancelEditCustomer(): void {
    this.editingCustomerId = null;
  }

  updateCustomer(id: number): void {
    this.saving = true;
    this.salesApiService.updateCustomer(id, this.editCustomer).subscribe({
      next: () => {
        this.editingCustomerId = null;
        this.saving = false;
        this.loadCustomers();
      },
      error: (error) => {
        console.error('Failed to update customer:', error);
        this.saving = false;
        this.cdr.detectChanges();
      },
    });
  }

  get filteredCustomers(): Customer[] {
    const search = this.customerSearchTerm.trim().toLowerCase();

    return this.customers.filter((customer) => {
      const matchesSearch =
        !search ||
        customer.name.toLowerCase().includes(search) ||
        customer.email.toLowerCase().includes(search) ||
        (customer.phone ?? '').toLowerCase().includes(search) ||
        (customer.website ?? '').toLowerCase().includes(search) ||
        (customer.address ?? '').toLowerCase().includes(search);

      const matchesStatus =
        !this.customerStatusFilter || customer.status === this.customerStatusFilter;

      return matchesSearch && matchesStatus;
    });
  }

  clearCustomerFilters(): void {
    this.customerSearchTerm = '';
    this.customerStatusFilter = '';
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
