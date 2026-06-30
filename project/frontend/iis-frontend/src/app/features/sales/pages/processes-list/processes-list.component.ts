import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { extractBackendErrorMessage } from '../../../../core/http-error-message';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';
import { SalesApiService } from '../../api/sales-api.service';
import { Customer } from '../../models/customer.model';
import { SalesProcess, SalesProcessRequest } from '../../models/sales-process.model';
import { AuthService } from '../../../../core/auth/auth.service';
import { SalesWorkflow } from '../../models/sales-workflow.model';

@Component({
  selector: 'app-processes-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './processes-list.component.html',
  styleUrls: ['./processes-list.component.css'],
})
export class ProcessesListComponent implements OnInit, OnDestroy {
  private readonly salesApiService = inject(SalesApiService);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);
  private readonly transientMessages = inject(TransientMessageService);

  processes: SalesProcess[] = [];
  customers: Customer[] = [];
  workflows: SalesWorkflow[] = [];

  loading = true;
  saving = false;
  errorMessage = '';
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

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  loadData(): void {
    this.loading = true;
    this.clearError();

    this.salesApiService.getSalesProcesses().pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }),
    ).subscribe({
      next: (response) => {
        this.processes = response ?? [];
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to load sales processes.'));
      },
    });

    this.salesApiService.getCustomers().subscribe({
      next: (response) => {
        this.customers = response ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to load customers.'));
      },
    });

    this.salesApiService.getSalesWorkflows().subscribe({
      next: (response) => {
        this.workflows = response ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to load sales workflows.'));
      },
    });
  }

  createProcess(): void {
    this.saving = true;
    this.clearError();

    this.salesApiService.createSalesProcess(this.newProcess).pipe(
      finalize(() => {
        this.saving = false;
        this.cdr.detectChanges();
      }),
    ).subscribe({
      next: () => {
        this.newProcess = {
          customerId: 0,
          title: '',
          workflowId: 0,
        };
        this.showCreateForm = false;
        this.loadData();
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to create sales process.'));
      },
    });
  }

  viewDetails(process: SalesProcess): void {
    this.router.navigate(['/sales/processes', process.id]);
  }

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private clearError(): void {
    this.transientMessages.clearField(this, 'errorMessage', () => this.cdr.detectChanges());
  }
}