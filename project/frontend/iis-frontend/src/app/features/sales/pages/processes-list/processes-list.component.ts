import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Customer } from '../../models/customer.model';
import { SalesProcess, SalesProcessRequest } from '../../models/sales-process.model';
import { AuthService } from '../../../../core/auth/auth.service';
import { SalesWorkflow } from '../../models/sales-workflow.model';
import { Router } from '@angular/router';
import { SalesApiService } from '../../api/sales-api.service';

@Component({
  selector: 'app-processes-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './processes-list.component.html',
  styleUrls: ['./processes-list.component.css'],
})
export class ProcessesListComponent implements OnInit {
  private readonly salesApiService = inject(SalesApiService);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);

  processes: SalesProcess[] = [];
  customers: Customer[] = [];
  workflows: SalesWorkflow[] = [];

  loading = true;
  saving = false;
  showCreateForm = false;

  canCreateProcess = false;

  newProcess: SalesProcessRequest = {
    customerId: 0,
    title: '',
    workflowId: 0,
  };

  ngOnInit(): void {
    this.canCreateProcess = this.authService.hasRole('ROLE_SALES_REPRESENTATIVE');
    this.loadData();
  }

  loadData(): void {
    this.loading = true;

    this.salesApiService.getSalesProcesses().subscribe({
      next: (response) => {
        this.processes = response ?? [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load sales processes:', error);
        this.loading = false;
        this.cdr.detectChanges();
      },
    });

    this.salesApiService.getCustomers().subscribe({
      next: (response) => {
        this.customers = response ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load customers:', error);
      },
    });

    this.salesApiService.getSalesWorkflows().subscribe({
      next: (response) => {
        this.workflows = response ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load sales workflows:', error);
      },
    });
  }

  createProcess(): void {
    this.saving = true;

    this.salesApiService.createSalesProcess(this.newProcess).subscribe({
      next: () => {
        this.newProcess = {
          customerId: 0,
          title: '',
          workflowId: 0,
        };
        this.showCreateForm = false;
        this.saving = false;
        this.loadData();
      },
      error: (error) => {
        console.error('Failed to create sales process:', error);
        this.saving = false;
        this.cdr.detectChanges();
      },
    });
  }

  viewDetails(process: SalesProcess): void {
    this.router.navigate(['/sales/processes', process.id]);
  }
}
