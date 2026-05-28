import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { SalesApiService } from '../../api/sales-api.service';
import { Customer } from '../../models/customer.model';
import {
  SalesProcess,
  SalesProcessRequest,
  SalesStage,
} from '../../models/sales-process.model';
import { AuthService } from '../../../../core/auth/auth.service';

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

  processes: SalesProcess[] = [];
  customers: Customer[] = [];

  loading = true;
  saving = false;
  showCreateForm = false;

  canManageProcesses = false;
  canCreateProcess = false;

  stages: SalesStage[] = [
    'NEW',
    'CONTACTED',
    'QUALIFIED',
    'PROPOSAL_SENT',
    'NEGOTIATION',
    'WON',
    'LOST',
  ];

  newProcess: SalesProcessRequest = {
    customerId: 0,
    title: '',
  };

  ngOnInit(): void {
    this.canManageProcesses =
        this.authService.hasRole('ROLE_SALES_REPRESENTATIVE') ||
        this.authService.hasRole('ROLE_SALES_MANAGER');
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
  }

  createProcess(): void {
    this.saving = true;

    this.salesApiService.createSalesProcess(this.newProcess).subscribe({
      next: () => {
        this.newProcess = {
          customerId: 0,
          title: '',
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

  updateStage(process: SalesProcess, stage: SalesStage): void {
    this.salesApiService.updateSalesProcessStage(process.id, { stage }).subscribe({
      next: () => this.loadData(),
      error: (error) => console.error('Failed to update stage:', error),
    });
  }
}