import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';

import { SalesApiService } from '../../api/sales-api.service';
import { FormsModule } from '@angular/forms';
import { Lead, LeadRequest } from '../../models/lead.model';
import { AuthService } from '../../../../core/auth/auth.service';

@Component({
  selector: 'app-leads-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './leads-list.component.html',
  styleUrls: ['./leads-list.component.css'],
})
export class LeadsListComponent implements OnInit {
  private readonly salesApiService = inject(SalesApiService);
  private readonly authService = inject(AuthService);

  private readonly cdr = inject(ChangeDetectorRef);

  leads: Lead[] = [];
  loading = true;

  showCreateForm = false;
  saving = false;

  canManageLeads = false;

  newLead: LeadRequest = {
    name: '',
    email: '',
    address: '',
    source: '',
    score: 0,
  };

  loadLeads(): void {
    this.loading = true;

    this.salesApiService.getLeads().subscribe({
        next: (response) => {
        this.leads = response ?? [];
        this.loading = false;
        this.cdr.detectChanges();
        },
        error: (error) => {
        console.error('Failed to load leads:', error);
        this.loading = false;
        this.cdr.detectChanges();
        },
    });
    }

  ngOnInit(): void {
    this.canManageLeads = this.authService.hasRole('ROLE_SALES_REPRESENTATIVE');
    this.loadLeads();
  }

    createLead(): void {
    this.saving = true;

    this.salesApiService.createLead(this.newLead).subscribe({
        next: () => {
        this.newLead = {
            name: '',
            email: '',
            address: '',
            source: '',
            score: 0,
        };
        this.showCreateForm = false;
        this.saving = false;
        this.loadLeads();
        },
        error: (error) => {
        console.error('Failed to create lead:', error);
        this.saving = false;
        this.cdr.detectChanges();
        },
    });
    }

    qualifyLead(id: number): void {
        this.salesApiService.qualifyLead(id).subscribe({
            next: () => this.loadLeads(),
            error: (error) => console.error('Failed to qualify lead:', error),
        });
    }

    convertLead(id: number): void {
        this.salesApiService.convertLead(id).subscribe({
            next: () => this.loadLeads(),
            error: (error) => console.error('Failed to convert lead:', error),
        });
    }
}