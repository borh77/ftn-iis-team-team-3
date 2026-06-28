export type LeadStatus = 'NEW' | 'QUALIFIED' | 'CONVERTED';

export interface Lead {
  id: number;
  name: string;
  email: string;
  address: string;
  source: string;
  score: number;
  regionId: number | null;
  regionName: string | null;
  status: LeadStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface LeadRequest {
  name: string;
  email: string;
  address: string;
  source: string;
  score: number;
  regionId: number | null;
}