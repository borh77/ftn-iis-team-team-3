export type SalesStage =
  | 'NEW'
  | 'CONTACTED'
  | 'QUALIFIED'
  | 'PROPOSAL_SENT'
  | 'NEGOTIATION'
  | 'WON'
  | 'LOST';

export type SalesProcessStatus = 'ACTIVE' | 'CLOSED';
export type SalesProcessOutcome = 'OPEN' | 'WON' | 'LOST';

export interface SalesProcess {
  id: number;
  customerId: number;
  customerName: string;
  title: string;
  stage: SalesStage;
  status: SalesProcessStatus;
  outcome: SalesProcessOutcome;
  createdAt: string;
  updatedAt: string;
}

export interface SalesProcessRequest {
  customerId: number;
  title: string;
}

export interface UpdateSalesStageRequest {
  stage: SalesStage;
}