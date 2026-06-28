export interface QuantityThreshold {
  quantityFrom: number;
  quantityTo: number | null;
  price: number;
}

export interface PricelistItem {
  id?: number;
  variantId: number;
  variantName: string;
  activeVariant: boolean;
  replacementRequired: boolean;
  catalogAvailable: boolean;
  thresholds: QuantityThreshold[];
}

export interface Pricelist {
  id: number;
  regionId: number;
  regionName: string;
  customerSegment: string;
  currency: string;
  status: 'DRAFT' | 'IN_REVIEW' | 'ACTIVE' | 'ARCHIVED';
  versionNumber: number;
  parentPricelistId: number | null;
  rootPricelistId: number | null;
  canCreateNewVersion: boolean;
  owner: boolean;
  canCollaborate: boolean;
  canManageOffers: boolean;
  periodStart: string;
  periodEnd: string;
  items: PricelistItem[];
}

export interface CreatePricelistPayload {
  regionId: number;
  customerSegment: string;
  currency: string;
  periodStart: string;
  periodEnd: string;
  items: PricelistItem[];
}

export interface ChangePricelistStatusPayload {
  targetStatus: Pricelist['status'];
  reason?: string;
}

export interface CreatePricelistErrorResponse {
  error?: string;
}

export type PricelistCreationStep =
  | 'BASIC_INFO'
  | 'TEAM_ACCESS'
  | 'ITEMS'
  | 'THRESHOLDS'
  | 'REVIEW'
  | 'COMPLETED';

export interface PricelistWizardState {
  pricelistId: number;
  creationStep: PricelistCreationStep;
  creationCompleted: boolean;
  status: Pricelist['status'];
  teamId: number | null;
  teamName: string | null;
  lastEditedAt: string | null;
  pricelist: Pricelist | null;
}

export interface StartPricelistWizardResponse {
  pricelistId: number;
  state: PricelistWizardState;
}

export interface SaveBasicInfoStepRequest {
  regionId: number;
  customerSegment: string;
  currency: string;
  periodStart: string;
  periodEnd: string;
}

export interface SaveTeamAccessStepRequest {
  teamId: number | null;
}

export interface SaveItemsStepRequest {
  items: Array<{
    variantId: number;
    variantName: string;
  }>;
}

export interface SaveThresholdsStepRequest {
  items: Array<{
    variantId: number;
    thresholds: QuantityThreshold[];
  }>;
}

export interface PricelistWizardSummary {
  pricelistId: number;
  readyToFinish: boolean;
  validationMessages: string[];
  pricelist: Pricelist | null;
}
