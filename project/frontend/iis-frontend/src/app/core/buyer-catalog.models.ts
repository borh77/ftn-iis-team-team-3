export interface BuyerCatalogThreshold {
  quantityFrom: number;
  quantityTo?: number | null;
  price: number;
}

export interface BuyerCatalogItem {
  variantId: number;
  variantName: string;
  basePrice: number;
  discountedPrice?: number | null;
  discountType?: 'PERCENTAGE' | 'FIXED_AMOUNT' | null;
  discountValue?: number | null;
  hasActiveOffer: boolean;
  currency: string;
  thresholds: BuyerCatalogThreshold[];
}

export interface BuyerCatalog {
  pricelistId: number;
  regionName: string;
  customerSegment: string;
  currency: string;
  periodStart: string;
  periodEnd: string;
  items: BuyerCatalogItem[];
}
