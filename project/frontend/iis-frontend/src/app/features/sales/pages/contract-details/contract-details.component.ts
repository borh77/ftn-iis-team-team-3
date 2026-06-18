import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { SalesApiService } from '../../api/sales-api.service';
import { Contract } from '../../models/contract.model';

@Component({
  selector: 'app-contract-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './contract-details.component.html',
  styleUrl: './contract-details.component.css',
})
export class ContractDetailsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly salesApiService = inject(SalesApiService);
  private readonly cdr = inject(ChangeDetectorRef);

  contract?: Contract;
  loading = true;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.salesApiService.getContractById(id).subscribe({
      next: (response) => {
        this.contract = response;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load contract:', error);
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/sales/contracts']);
  }

  openProcess(): void {
    if (this.contract) {
      this.router.navigate(['/sales/processes', this.contract.salesProcessId]);
    }
  }
}