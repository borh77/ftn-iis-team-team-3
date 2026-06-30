import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { SalesApiService } from '../../api/sales-api.service';
import { Contract } from '../../models/contract.model';
import { extractBackendErrorMessage } from '../../../../core/http-error-message';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';

@Component({
  selector: 'app-contracts-list',
  standalone: true,
  imports: [CommonModule],
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
  signingContractId: number | null = null;
  errorMessage = '';

  ngOnInit(): void {
    this.loadContracts();
  }

  loadContracts(): void {
    this.loading = true;
    this.clearError();

    this.salesApiService.getContracts().pipe(finalize(() => (this.loading = false))).subscribe({
      next: (response) => {
        this.contracts = response ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to load contracts.'));
        this.cdr.detectChanges();
      },
    });
  }

  signContract(contract: Contract): void {
    this.signingContractId = contract.id;
    this.clearError();
    this.salesApiService.signContract(contract.id).pipe(finalize(() => (this.signingContractId = null))).subscribe({
      next: () => this.loadContracts(),
      error: (error) => this.showError(extractBackendErrorMessage(error, 'Failed to sign contract.')),
    });
  }

  viewDetails(contract: Contract): void {
    this.router.navigate(['/sales/contracts', contract.id]);
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
