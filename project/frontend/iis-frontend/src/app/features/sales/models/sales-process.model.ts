export type SalesStage = string;

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
  workflowId: number | null;
  workflowName: string | null;
}

export interface SalesProcessRequest {
  customerId: number;
  title: string;
  workflowId: number;
}

export interface UpdateSalesStageRequest {
  stage: SalesStage;
}