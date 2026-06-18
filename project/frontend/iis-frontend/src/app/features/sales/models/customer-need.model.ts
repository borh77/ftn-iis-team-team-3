export interface CustomerNeed {
  id: number;
  customerId: number;
  customerName: string;
  salesProcessId: number;
  salesProcessTitle: string;
  description: string;
  priority?: string;
  createdAt: string;
}

export interface CreateCustomerNeedRequest {
  salesProcessId: number;
  description: string;
  priority?: string;
}