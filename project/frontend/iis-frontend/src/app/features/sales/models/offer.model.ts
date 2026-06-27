export type OfferStatus = 'DRAFT' | 'SENT' | 'ACCEPTED' | 'REJECTED';

export interface OfferItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface Offer {
  id: number;
  offerNumber: string;
  customerId: number;
  customerName: string;
  salesProcessId: number;
  salesProcessTitle: string;
  status: OfferStatus;
  validUntil: string;
  totalAmount: number;
  notes?: string;
  createdAt: string;
  items: OfferItem[];
}

export interface CreateOfferItemRequest {
  productId: number;
  quantity: number;
  unitPrice: number;
}

export interface CreateOfferRequest {
  customerId: number;
  salesProcessId: number;
  validUntil: string;
  notes?: string;
  items: CreateOfferItemRequest[];
}

export interface UpdateOfferRequest {
  validUntil: string;
  notes?: string;
}