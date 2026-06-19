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
