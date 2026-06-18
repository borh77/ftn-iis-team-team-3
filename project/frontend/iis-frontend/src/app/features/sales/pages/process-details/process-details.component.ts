import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/auth/auth.service';

import { SalesApiService } from '../../api/sales-api.service';
import { SalesProcess } from '../../models/sales-process.model';
import { CustomerNeed } from '../../models/customer-need.model';
import { CustomerCommunication } from '../../models/customer-communication.model';
import { Offer } from '../../models/offer.model';
import { Contract } from '../../models/contract.model';
import { SalesProcessHistory } from '../../models/sales-process-history.model';
import { CustomerCommunicationRequest } from '../../models/customer-communication.model';

@Component({
  selector: 'app-process-details',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './process-details.component.html',
  styleUrl: './process-details.component.css',
})
export class ProcessDetailsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly salesApiService = inject(SalesApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);

  process?: SalesProcess;
  needs: CustomerNeed[] = [];
  communications: CustomerCommunication[] = [];
  offers: Offer[] = [];
  contracts: Contract[] = [];
  history: SalesProcessHistory[] = [];

  loading = true;
  showCommunicationForm = false;
  canManageCommunications = false;

  stages = ['NEW', 'CONTACTED', 'QUALIFIED', 'PROPOSAL_SENT', 'NEGOTIATION', 'WON', 'LOST'];

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.canManageCommunications =
     this.authService.hasRole('ROLE_ACCOUNT_MANAGER');

    this.salesApiService.getSalesProcessById(id).subscribe({
      next: (process) => {
        this.process = process;

        forkJoin({
          needs: this.salesApiService.getCustomerNeeds(process.customerId),
          communications: this.salesApiService.getCustomerCommunications(process.customerId),
          offers: this.salesApiService.getOffers(),
          contracts: this.salesApiService.getContracts(),
          history: this.salesApiService.getSalesProcessHistory(process.id),
        }).subscribe({
          next: (data) => {
            this.needs = data.needs.filter((need) => need.salesProcessId === process.id);
            this.communications = data.communications;
            this.offers = data.offers.filter((offer) => offer.salesProcessId === process.id);
            this.contracts = data.contracts.filter((contract) => contract.salesProcessId === process.id);
            this.history = data.history;

            this.loading = false;
            this.cdr.detectChanges();
          },
          error: (error) => {
            console.error('Failed to load process details:', error);
            this.loading = false;
            this.cdr.detectChanges();
          },
        });
      },
      error: (error) => {
        console.error('Failed to load process:', error);
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  newCommunication: CustomerCommunicationRequest = {
    type: 'MEETING',
    communicationDate: '',
    summary: '',
  };

  addCommunication(): void {
    if (!this.canManageCommunications || !this.process) {
        return;
    }

    this.salesApiService
        .createCustomerCommunication(this.process.customerId, this.newCommunication)
        .subscribe({
        next: () => {
            this.showCommunicationForm = false;
            this.newCommunication = {
            type: 'MEETING',
            communicationDate: '',
            summary: '',
            };

            this.ngOnInit();
        },
        error: (error) => console.error('Failed to add communication:', error),
        });
    }

  goBack(): void {
    this.router.navigate(['/sales/processes']);
  }

  isStageReached(stage: string): boolean {
    if (!this.process) {
      return false;
    }

    return this.stages.indexOf(stage) <= this.stages.indexOf(this.process.stage);
  }
}