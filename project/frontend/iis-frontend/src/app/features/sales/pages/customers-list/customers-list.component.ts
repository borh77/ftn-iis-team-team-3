import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { SalesApiService } from '../../api/sales-api.service';
import { Customer, CustomerRequest } from '../../models/customer.model';
import { AuthService } from '../../../../core/auth/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-customers-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './customers-list.component.html',
  styleUrls: ['./customers-list.component.css'],
})
export class CustomersListComponent implements OnInit {
  private readonly salesApiService = inject(SalesApiService);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);

  customers: Customer[] = [];
  loading = true;
  saving = false;
  showCreateForm = false;
  canManageCustomers = false;

  editingCustomerId: number | null = null;

  newCustomer: CustomerRequest = {
    name: '',
    email: '',
    phone: '',
    website: '',
    address: '',
  };

  editCustomer: CustomerRequest = {
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

    this.salesApiService.getCustomers().subscribe({
      next: (response) => {
        this.customers = response ?? [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load customers:', error);
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  viewDetails(customer: Customer): void {
    this.router.navigate(['/sales/customers', customer.id]);
  }

  createCustomer(): void {
    this.saving = true;

    this.salesApiService.createCustomer(this.newCustomer).subscribe({
      next: () => {
        this.newCustomer = {
          name: '',
          email: '',
          phone: '',
          website: '',
          address: '',
        };
        this.showCreateForm = false;
        this.saving = false;
        this.loadCustomers();
      },
      error: (error) => {
        console.error('Failed to create customer:', error);
        this.saving = false;
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
}