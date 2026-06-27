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

export interface ReplacementSuggestion {
  oldVariantId: number;
  oldVariantName?: string | null;
  newVariantId: number;
  newVariantName?: string | null;
  requestedQuantity: number;
  currentUnitPrice: number;
  discountAmount?: number | null;
  discountPercentage?: number | null;
  finalUnitPrice: number;
  lineTotal: number;
  message?: string | null;
}

export interface ValidationResult {
  valid: boolean;
  totalPrice: number;
  validatedItems: ValidatedOrderItem[];
  invalidItems: InvalidOrderItem[];
  replacements: ReplacementSuggestion[];
}
