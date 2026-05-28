import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/api.token';
import { Lead, LeadRequest } from '../models/lead.model';
import { Customer, CustomerRequest } from '../models/customer.model';
import { SalesProcess, SalesProcessRequest, UpdateSalesStageRequest } from '../models/sales-process.model';
import { CustomerCommunication, CustomerCommunicationRequest } from '../models/customer-communication.model';

@Injectable({
  providedIn: 'root',
})
export class SalesApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getLeads(): Observable<Lead[]> {
    return this.http.get<Lead[]>(
      `${this.apiBaseUrl}/api/sales/leads`,
    );
  }

  createLead(request: LeadRequest): Observable<Lead> {
    return this.http.post<Lead>(
        `${this.apiBaseUrl}/api/sales/leads`,
        request,
    );
  }

  qualifyLead(id: number): Observable<Lead> {
    return this.http.patch<Lead>(
        `${this.apiBaseUrl}/api/sales/leads/${id}/qualify`,
        {},
    );
  }

  convertLead(id: number): Observable<unknown> {
    return this.http.patch(
        `${this.apiBaseUrl}/api/sales/leads/${id}/convert`,
        {},
    );
  }

  getCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(
        `${this.apiBaseUrl}/api/sales/customers`,
    );
  }

  createCustomer(request: CustomerRequest): Observable<Customer> {
    return this.http.post<Customer>(
        `${this.apiBaseUrl}/api/sales/customers`,
        request,
    );
  }

  getSalesProcesses(): Observable<SalesProcess[]> {
    return this.http.get<SalesProcess[]>(
        `${this.apiBaseUrl}/api/sales/processes`,
    );
  }

  createSalesProcess(request: SalesProcessRequest): Observable<SalesProcess> {
    return this.http.post<SalesProcess>(
        `${this.apiBaseUrl}/api/sales/processes`,
        request,
    );
  }

  updateSalesProcessStage(
    id: number,
    request: UpdateSalesStageRequest,
    ): Observable<SalesProcess> {
    return this.http.patch<SalesProcess>(
        `${this.apiBaseUrl}/api/sales/processes/${id}/stage`,
        request,
    );
  }

  getCustomerCommunications(customerId: number): Observable<CustomerCommunication[]> {
    return this.http.get<CustomerCommunication[]>(
        `${this.apiBaseUrl}/api/sales/customers/${customerId}/communications`,
    );
  }

  createCustomerCommunication(
    customerId: number,
    request: CustomerCommunicationRequest,
    ): Observable<CustomerCommunication> {
    return this.http.post<CustomerCommunication>(
        `${this.apiBaseUrl}/api/sales/customers/${customerId}/communications`,
        request,
    );
  }
}