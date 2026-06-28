export type CustomerStatus = 'ACTIVE' | 'INACTIVE';

export interface Customer {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  website: string | null;
  address: string | null;
  regionId: number | null;
  regionName: string | null;
  status: CustomerStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface CustomerRequest {
  name: string;
  email: string;
  phone: string;
  website: string;
  address: string;
  regionId: number | null;
}