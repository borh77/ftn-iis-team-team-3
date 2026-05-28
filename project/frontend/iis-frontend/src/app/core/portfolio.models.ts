export interface Category {
  id: number;
  name: string;
  description: string | null;
  status: 'ACTIVE' | 'ARCHIVED';
}

export interface Subcategory {
  id: number;
  categoryId: number;
  categoryName: string;
  name: string;
  description: string | null;
  status: 'ACTIVE' | 'ARCHIVED';
}

export interface Product {
  id: number;
  name: string;
  description: string | null;
  subcategoryId: number;
  subcategoryName: string;
  therapeuticAreaId: number;
  therapeuticAreaName: string;
  status: 'ACTIVE' | 'ARCHIVED';
}

export interface Variant {
  id: number;
  productId: number;
  productName: string;
  form: string;
  dosage: string;
  status: 'ACTIVE' | 'ARCHIVED';
}