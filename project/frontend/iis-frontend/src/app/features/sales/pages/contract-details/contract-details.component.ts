import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';

import { SalesApiService } from '../../api/sales-api.service';
import { Contract } from '../../models/contract.model';
import { extractBackendErrorMessage } from '../../../../core/http-error-message';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';

@Component({
  selector: 'app-contract-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './contract-details.component.html',
  styleUrl: './contract-details.component.css',
})
export class ContractDetailsComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly salesApiService = inject(SalesApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly transientMessages = inject(TransientMessageService);

  contract?: Contract;
  loading = true;
  signing = false;
  errorMessage = '';

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.salesApiService.getContractById(id).pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }),
    ).subscribe({
      next: (response) => {
        this.contract = response;
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to load contract.'));
      },
    });
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

  goBack(): void {
    this.router.navigate(['/sales/contracts']);
  }

  openProcess(): void {
    if (this.contract) {
      this.router.navigate(['/sales/processes', this.contract.salesProcessId]);
    }
  }

  signContract(): void {
    if (!this.contract || this.contract.status !== 'PENDING') {
      return;
    }

    this.signing = true;
    this.clearError();

    this.salesApiService.signContract(this.contract.id).pipe(
      finalize(() => {
        this.signing = false;
        this.cdr.detectChanges();
      }),
    ).subscribe({
      next: (response) => {
        this.contract = response;
      },
      error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to sign contract.'));
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