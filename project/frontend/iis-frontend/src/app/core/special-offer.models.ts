export type DiscountType = 'PERCENTAGE' | 'FIXED_AMOUNT';
export type SpecialOfferStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED';

export interface SpecialOffer {
  id: number;
  pricelistId: number;
  variantId: number;
  variantName: string;
  discountType: DiscountType;
  discountValue: number;
  startDate: string;
  endDate: string;
  status: SpecialOfferStatus;
}

export interface CreateSpecialOfferPayload {
  pricelistId: number;
  variantId: number;
  discountType: DiscountType;
  discountValue: number;
  startDate: string;
  endDate: string;
}

export interface PromotionSuggestion {
  variantId?: number | null;
  brandId?: number | null;
  targetName: string;
  customerSegment: string;
  suggestedDiscountType: DiscountType;
  suggestedDiscountValue: number;
  reason: string;
  expectedEffect?: string | null;
  source?: string | null;
}
