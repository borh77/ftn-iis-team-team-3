export interface BuyerCatalogThreshold {
  quantityFrom: number;
  quantityTo: number | null;
  price: number;
}

export interface BuyerCatalogItem {
  variantId: number;
  variantName: string;
  basePrice: number | null;
  discountedPrice: number | null;
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT' | null;
  discountValue: number | null;
  hasActiveOffer: boolean;
  currency: string;
  thresholds: BuyerCatalogThreshold[];
}

export interface BuyerCatalog {
  pricelistId: number | null;
  regionName: string | null;
  customerSegment: string | null;
  currency: string | null;
  periodStart: string | null;
  periodEnd: string | null;
  items: BuyerCatalogItem[];
}
