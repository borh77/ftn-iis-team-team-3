export interface QuantityThreshold {
  quantityFrom: number;
  quantityTo: number | null;
  price: number;
}

export interface PricelistItem {
  id?: number;
  variantId: number;
  variantName: string;
  thresholds: QuantityThreshold[];
}

export interface Pricelist {
  id: number;
  regionId: number;
  regionName: string;
  customerSegment: string;
  currency: string;
  status: 'DRAFT' | 'IN_REVIEW' | 'ACTIVE' | 'ARCHIVED';
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
