import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { SalesApiService } from '../../api/sales-api.service';
import { Contract } from '../../models/contract.model';
import { extractBackendErrorMessage } from '../../../../core/http-error-message';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';

@Component({
  selector: 'app-contracts-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './contracts-list.component.html',
  styleUrls: ['./contracts-list.component.css'],
})
export class ContractsListComponent implements OnInit, OnDestroy {
  private readonly salesApiService = inject(SalesApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);
  private readonly transientMessages = inject(TransientMessageService);

  contracts: Contract[] = [];
  loading = true;
  errorMessage = '';

  saving = false;
  editingContractId: number | null = null;

  editContract = {
    startDate: '',
    endDate: '',
    terms: '',
  };

  ngOnInit(): void {
    this.loadContracts();
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  loadContracts(): void {
    this.loading = true;
    this.clearError();

    this.salesApiService.getContracts().pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }),
    ).subscribe({
      next: (response) => {
        this.contracts = response ?? [];
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to load contracts.'));
      },
    });
  }

  viewDetails(contract: Contract): void {
    this.router.navigate(['/sales/contracts', contract.id]);
  }

  startEditContract(contract: Contract): void {
    this.editingContractId = contract.id;
    this.editContract = {
      startDate: contract.startDate,
      endDate: contract.endDate,
      terms: contract.terms ?? '',
    };
  }

  cancelEditContract(): void {
    this.editingContractId = null;
    this.editContract = {
      startDate: '',
      endDate: '',
      terms: '',
    };
  }

  updateContract(contract: Contract): void {
    this.saving = true;
    this.clearError();

    this.salesApiService.updateContract(contract.id, this.editContract).pipe(
      finalize(() => {
        this.saving = false;
        this.cdr.detectChanges();
      }),
    ).subscribe({
      next: () => {
        this.cancelEditContract();
        this.loadContracts();
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to update contract.'));
      },
    });
  }

  private showError(message: string): void {
    this.transientMessages.setField(this, 'errorMessage', message, ERROR_MESSAGE_MS, () => this.cdr.detectChanges());
  }

  private clearError(): void {
    this.transientMessages.clearField(this, 'errorMessage', () => this.cdr.detectChanges());
  }
}
