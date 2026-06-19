export type ContractStatus = 'DRAFT' | 'PENDING' | 'SIGNED' | 'CANCELLED';

export interface Contract {
  id: number;
  contractNumber: string;
  offerId: number;
  offerNumber: string;
  customerId: number;
  customerName: string;
  salesProcessId: number;
  salesProcessTitle: string;
  status: ContractStatus;
  startDate: string;
  endDate: string;
  totalValue: number;
  terms?: string;
  createdAt: string;
  signedAt?: string;
}

export interface CreateContractRequest {
  offerId: number;
  startDate: string;
  endDate: string;
  terms?: string;
}