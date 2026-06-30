export interface SalesWorkflow {
  id: number;
  name: string;
  regionId: number | null;
  regionName: string;
  regionCode: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  stages: SalesStageDefinition[];
}

export interface CreateSalesWorkflowRequest {
  name: string;
  regionId: number | null;
}

export interface SalesStageDefinition {
  id: number;
  workflowId: number;
  name: string;
  description: string;
  stageOrder: number;
  startStage: boolean;
  endStage: boolean;
  successfulEnd: boolean;
  requiredInputs: string;
  expectedOutputs: string;
}

export interface SalesStageTransition {
  id: number;
  workflowId: number;
  fromStageId: number;
  fromStageName: string;
  toStageId: number;
  toStageName: string;
  conditionType: string;
  conditionDescription: string;
}

export interface CreateSalesStageRequest {
  name: string;
  description: string;
  stageOrder: number;
  startStage: boolean;
  endStage: boolean;
  successfulEnd: boolean;
  requiredInputs: string;
  expectedOutputs: string;
}

export interface CreateSalesStageTransitionRequest {
  fromStageId: number;
  toStageId: number;
  conditionType: string;
  conditionDescription: string;
}