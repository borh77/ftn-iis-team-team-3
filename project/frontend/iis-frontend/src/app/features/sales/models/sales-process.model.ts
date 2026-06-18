export type SalesStage =
  | 'NEW'
  | 'CONTACTED'
  | 'QUALIFIED'
  | 'PROPOSAL_SENT'
  | 'NEGOTIATION'
  | 'WON'
  | 'LOST';

export type SalesProcessStatus = 'ACTIVE' | 'SUCCESSFUL' | 'UNSUCCESSFUL';
export type SalesProcessOutcome = 'OPEN' | 'CLOSED_WON' | 'CLOSED_LOST';

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