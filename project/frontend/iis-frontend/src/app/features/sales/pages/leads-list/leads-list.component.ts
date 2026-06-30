import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';

import { RegionService } from '../../../../core/region.service';
import { Region } from '../../../../core/region.model';
import { SalesApiService } from '../../api/sales-api.service';
import { FormsModule } from '@angular/forms';
import { Lead, LeadRequest } from '../../models/lead.model';
import { AuthService } from '../../../../core/auth/auth.service';
import { finalize } from 'rxjs';
import { extractBackendErrorMessage } from '../../../../core/http-error-message';
import { ERROR_MESSAGE_MS, TransientMessageService } from '../../../../core/transient-message.service';

@Component({
  selector: 'app-leads-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './leads-list.component.html',
  styleUrls: ['./leads-list.component.css'],
})
export class LeadsListComponent implements OnInit, OnDestroy {
  private readonly salesApiService = inject(SalesApiService);
  private readonly authService = inject(AuthService);
  private readonly regionService = inject(RegionService);
  private readonly transientMessages = inject(TransientMessageService);

  private readonly cdr = inject(ChangeDetectorRef);

  leads: Lead[] = [];
  loading = true;
  errorMessage = '';

  showCreateForm = false;
  saving = false;

  canManageLeads = false;

  editingLeadId: number | null = null;

  leadSearchTerm = '';
  leadStatusFilter = '';
  regions: Region[] = [];

  newLead: LeadRequest = {
    name: '',
    email: '',
    address: '',
    source: '',
    score: 0,
    regionId: null,
  };

  editLead: LeadRequest = {
    name: '',
    email: '',
    address: '',
    source: '',
    score: 0,
    regionId: null,
  };

  loadLeads(): void {
    this.loading = true;
    this.clearError();

    this.salesApiService.getLeads().pipe(finalize(() => (this.loading = false))).subscribe({
        next: (response) => {
        this.leads = response ?? [];
        this.cdr.detectChanges();
        },
        error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to load leads.'));
        this.cdr.detectChanges();
        },
    });
    }

  ngOnInit(): void {
    this.canManageLeads = this.authService.hasRole('ROLE_SALES_REPRESENTATIVE');
    this.loadLeads();
    this.loadRegions();
  }

  ngOnDestroy(): void {
    this.transientMessages.clearAll(this);
  }

    createLead(): void {
    this.saving = true;
    this.clearError();

    this.salesApiService.createLead(this.newLead).pipe(finalize(() => (this.saving = false))).subscribe({
        next: () => {
        this.newLead = {
            name: '',
            email: '',
            address: '',
            source: '',
            score: 0,
            regionId: null,
        };
        this.showCreateForm = false;
        this.loadLeads();
        },
        error: (error) => {
        this.showError(extractBackendErrorMessage(error, 'Failed to create lead.'));
        this.cdr.detectChanges();
        },
    });
    }

    qualifyLead(id: number): void {
        this.salesApiService.qualifyLead(id).subscribe({
            next: () => this.loadLeads(),
            error: (error) => this.showError(extractBackendErrorMessage(error, 'Failed to qualify lead.')),
        });
    }

    convertLead(id: number): void {
        this.salesApiService.convertLead(id).subscribe({
            next: () => this.loadLeads(),
            error: (error) => this.showError(extractBackendErrorMessage(error, 'Failed to convert lead.')),
        });
    }

    startEditLead(lead: Lead): void {
      this.editingLeadId = lead.id;
      this.editLead = {
        name: lead.name,
        email: lead.email,
        address: lead.address,
        source: lead.source,
        score: lead.score,
        regionId: lead.regionId,
      };
    }

    cancelEditLead(): void {
      this.editingLeadId = null;
    }

    updateLead(id: number): void {
      this.saving = true;
      this.salesApiService.updateLead(id, this.editLead).subscribe({
        next: () => {
          this.editingLeadId = null;
          this.saving = false;
          this.loadLeads();
        },
        error: (error) => {
          console.error('Failed to update lead:', error);
          this.saving = false;
          this.cdr.detectChanges();
        },
      });
    }

  get filteredLeads(): Lead[] {
    const search = this.leadSearchTerm.trim().toLowerCase();
    return this.leads.filter((lead) => {
      const matchesSearch =
        !search ||
        lead.name.toLowerCase().includes(search) ||
        lead.email.toLowerCase().includes(search) ||
        (lead.address ?? '').toLowerCase().includes(search) ||
        (lead.source ?? '').toLowerCase().includes(search) ||
        (lead.regionName ?? '').toLowerCase().includes(search);
      const matchesStatus =
        !this.leadStatusFilter || lead.status === this.leadStatusFilter;
      return matchesSearch && matchesStatus;
    });
  }

  clearLeadFilters(): void {
    this.leadSearchTerm = '';
    this.leadStatusFilter = '';
  }

  loadRegions(): void {
    this.regionService.list().subscribe({
      next: (response) => {
        this.regions = response ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load regions:', error);
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
