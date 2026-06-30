import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SalesApiService } from '../../api/sales-api.service';
import { Contract } from '../../models/contract.model';

@Component({
  selector: 'app-contracts-list',
  standalone: true,
  imports: [CommonModule, FormsModule],  
  templateUrl: './contracts-list.component.html',
  styleUrls: ['./contracts-list.component.css'],
})
export class ContractsListComponent implements OnInit {
  private readonly salesApiService = inject(SalesApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);

  contracts: Contract[] = [];
  loading = true;

  saving = false;
  editingContractId: number | null = null;

  editContract = {
    startDate: '',
    endDate: '',
    terms: '',
  };

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
  }

  updateContract(contract: Contract): void {
    this.saving = true;

    this.salesApiService.updateContract(contract.id, this.editContract).subscribe({
      next: () => {
        this.editingContractId = null;
        this.saving = false;
        this.loadContracts();
      },
      error: (error) => {
        console.error('Failed to update contract:', error);
        this.saving = false;
        this.cdr.detectChanges();
      },
    });
  }

  ngOnInit(): void {
    this.loadContracts();
  }

  loadContracts(): void {
    this.loading = true;

    this.salesApiService.getContracts().subscribe({
      next: (response) => {
        this.contracts = response ?? [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load contracts:', error);
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  viewDetails(contract: Contract): void {
    this.router.navigate(['/sales/contracts', contract.id]);
  }
}
