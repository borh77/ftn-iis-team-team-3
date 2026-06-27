import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/api.token';
import { Lead, LeadRequest } from '../models/lead.model';
import { Customer, CustomerRequest } from '../models/customer.model';
import { SalesProcess, SalesProcessRequest, UpdateSalesStageRequest, SalesStage } from '../models/sales-process.model';
import { CustomerCommunication, CustomerCommunicationRequest } from '../models/customer-communication.model';
import { CustomerNeed, CreateCustomerNeedRequest } from '../models/customer-need.model';
import { Offer, CreateOfferRequest } from '../models/offer.model';
import { Contract, CreateContractRequest, UpdateContractRequest } from '../models/contract.model';
import { SalesProcessHistory } from '../models/sales-process-history.model';
import { CreateSalesStageRequest, CreateSalesStageTransitionRequest, CreateSalesWorkflowRequest, SalesStageDefinition, SalesStageTransition, SalesWorkflow, } from '../models/sales-workflow.model';

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

  updateLead(id: number, request: LeadRequest): Observable<Lead> {
    return this.http.put<Lead>(
      `${this.apiBaseUrl}/api/sales/leads/${id}`,
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

  updateCustomer(id: number, request: CustomerRequest): Observable<Customer> {
    return this.http.put<Customer>(
      `${this.apiBaseUrl}/api/sales/customers/${id}`,
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

  getCustomerNeeds(customerId: number): Observable<CustomerNeed[]> {
    return this.http.get<CustomerNeed[]>(
      `${this.apiBaseUrl}/api/customers/${customerId}/needs`,
    );
  }

  createCustomerNeed(
    customerId: number,
    request: CreateCustomerNeedRequest,
  ): Observable<CustomerNeed> {
    return this.http.post<CustomerNeed>(
      `${this.apiBaseUrl}/api/customers/${customerId}/needs`,
      request,
    );
  }

  getOffers(): Observable<Offer[]> {
    return this.http.get<Offer[]>(
      `${this.apiBaseUrl}/api/offers`,
    );
  }

  createOffer(request: CreateOfferRequest): Observable<Offer> {
    return this.http.post<Offer>(
      `${this.apiBaseUrl}/api/offers`,
      request,
    );
  }

  acceptOffer(id: number): Observable<Offer> {
    return this.http.patch<Offer>(
      `${this.apiBaseUrl}/api/offers/${id}/accept`,
      {},
    );
  }

  getContracts(): Observable<Contract[]> {
    return this.http.get<Contract[]>(
      `${this.apiBaseUrl}/api/contracts`,
    );
  }

  createContract(request: CreateContractRequest): Observable<Contract> {
    return this.http.post<Contract>(
      `${this.apiBaseUrl}/api/contracts`,
      request,
    );
  }

  updateContract(id: number, request: UpdateContractRequest): Observable<Contract> {
    return this.http.put<Contract>(
      `${this.apiBaseUrl}/api/contracts/${id}`,
      request,
    );
  }

  signContract(id: number): Observable<Contract> {
    return this.http.patch<Contract>(
      `${this.apiBaseUrl}/api/contracts/${id}/sign`,
      {},
    );
  }

  getSalesProcessById(id: number): Observable<SalesProcess> {
    return this.http.get<SalesProcess>(
      `${this.apiBaseUrl}/api/sales/processes/${id}`,
    );
  }

  getSalesProcessHistory(id: number): Observable<SalesProcessHistory[]> {
    return this.http.get<SalesProcessHistory[]>(
      `${this.apiBaseUrl}/api/sales/processes/${id}/history`,
    );
  }

  getContractById(id: number): Observable<Contract> {
    return this.http.get<Contract>(
      `${this.apiBaseUrl}/api/contracts/${id}`,
    );
  }

  getAvailableStageTransitions(processId: number): Observable<SalesStage[]> {
    return this.http.get<SalesStage[]>(
      `${this.apiBaseUrl}/api/sales/processes/${processId}/available-transitions`,
    );
  }

  getSalesWorkflows(): Observable<SalesWorkflow[]> {
    return this.http.get<SalesWorkflow[]>(
      `${this.apiBaseUrl}/api/sales/workflows`,
    );
  }

  createSalesWorkflow(request: CreateSalesWorkflowRequest): Observable<SalesWorkflow> {
    return this.http.post<SalesWorkflow>(
      `${this.apiBaseUrl}/api/sales/workflows`,
      request,
    );
  }

  getSalesWorkflowStages(workflowId: number): Observable<SalesStageDefinition[]> {
    return this.http.get<SalesStageDefinition[]>(
      `${this.apiBaseUrl}/api/sales/workflows/${workflowId}/stages`,
    );
  }

  addSalesWorkflowStage(
    workflowId: number,
    request: CreateSalesStageRequest,
  ): Observable<SalesStageDefinition> {
    return this.http.post<SalesStageDefinition>(
      `${this.apiBaseUrl}/api/sales/workflows/${workflowId}/stages`,
      request,
    );
  }

  getSalesWorkflowTransitions(workflowId: number): Observable<SalesStageTransition[]> {
    return this.http.get<SalesStageTransition[]>(
      `${this.apiBaseUrl}/api/sales/workflows/${workflowId}/transitions`,
    );
  }

  addSalesWorkflowTransition(
    workflowId: number,
    request: CreateSalesStageTransitionRequest,
  ): Observable<SalesStageTransition> {
    return this.http.post<SalesStageTransition>(
      `${this.apiBaseUrl}/api/sales/workflows/${workflowId}/transitions`,
      request,
    );
  }
}