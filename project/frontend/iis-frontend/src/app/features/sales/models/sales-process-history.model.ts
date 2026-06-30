import { SalesStage } from './sales-process.model';

export interface SalesProcessHistory {
  id: number;
  salesProcessId: number;
  previousStage: SalesStage;
  newStage: SalesStage;
  changedAt: string;
  changedById?: number;
  changedByUsername?: string;
}
