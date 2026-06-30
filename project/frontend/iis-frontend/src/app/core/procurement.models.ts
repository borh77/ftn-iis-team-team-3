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
  variantName?: string | null;
  productName?: string | null;
  form?: string | null;
  dosage?: string | null;
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

export interface ConfirmProcurementItem {
  variantId: number;
  requestedQuantity: number;
  originalVariantId?: number | null;
  originalVariantName?: string | null;
  replacementAccepted: boolean;
}

export interface ConfirmProcurementRequest {
  sourceFileName?: string | null;
  items: ConfirmProcurementItem[];
}

export interface ProcurementOrderItem {
  id: number;
  originalVariantId?: number | null;
  originalVariantName?: string | null;
  variantId: number;
  variantName: string;
  requestedQuantity: number;
  unitPrice: number;
  discountType?: DiscountType | null;
  discountValue?: number | null;
  finalUnitPrice: number;
  lineTotal: number;
  replacementAccepted: boolean;
}

export interface ProcurementOrder {
  id: number;
  status: 'SUBMITTED' | 'CANCELLED' | 'FULFILLED';
  buyerName?: string | null;
  buyerUsername: string;
  regionName?: string | null;
  customerSegment: string;
  pricelistId?: number | null;
  sourceFileName?: string | null;
  totalPrice: number;
  currency?: string | null;
  createdAt: string;
  confirmedAt: string;
  items: ProcurementOrderItem[];
}
