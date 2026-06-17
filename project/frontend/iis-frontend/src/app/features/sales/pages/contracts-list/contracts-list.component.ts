import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';

import { SalesApiService } from '../../api/sales-api.service';
import { Contract } from '../../models/contract.model';

@Component({
  selector: 'app-contracts-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './contracts-list.component.html',
  styleUrls: ['./contracts-list.component.css'],
})
export class ContractsListComponent implements OnInit {
  private readonly salesApiService = inject(SalesApiService);
  private readonly cdr = inject(ChangeDetectorRef);

  contracts: Contract[] = [];
  loading = true;

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

  signContract(contract: Contract): void {
    this.salesApiService.signContract(contract.id).subscribe({
      next: () => this.loadContracts(),
      error: (error) => console.error('Failed to sign contract:', error),
    });
  }
}