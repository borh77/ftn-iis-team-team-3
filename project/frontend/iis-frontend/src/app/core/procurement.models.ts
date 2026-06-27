export type DiscountType = 'PERCENTAGE' | 'FIXED_AMOUNT';

export interface ValidatedOrderItem {
  variantId: number;
  variantName?: string | null;
  requestedQuantity: number;
  unitPrice: number;
  discountType?: DiscountType | null;
  discountValue?: number | null;
  finalUnitPrice: number;
  lineTotal: number;
}

export interface InvalidOrderItem {
  variantId?: number | null;
  requestedQuantity?: number | null;
  errorCode: string;
  message: string;
}

export interface ValidationResult {
  valid: boolean;
  totalPrice: number;
  validatedItems: ValidatedOrderItem[];
  invalidItems: InvalidOrderItem[];
}
