export type EntityStatus = 'ACTIVE' | 'ARCHIVED';

export type IngredientType = 'ACTIVE_SUBSTANCE' | 'EXCIPIENT';

export type VariantVersionStatus = 'DEVELOPMENT' | 'ACTIVE' | 'ARCHIVED';

export interface CategoryResponse {
  id: number;
  name: string;
  description: string | null;
  status: EntityStatus;
}

export interface CategoryRequest {
  name: string;
  description: string;
}

export interface SubcategoryResponse {
  id: number;
  categoryId: number;
  categoryName: string;
  name: string;
  description: string | null;
  status: EntityStatus;
}

export interface SubcategoryRequest {
  categoryId: number;
  name: string;
  description: string;
}

export interface TherapeuticAreaResponse {
  id: number;
  name: string;
  description: string | null;
  status: EntityStatus;
}

export interface TherapeuticAreaRequest {
  name: string;
  description: string;
}

export interface IngredientResponse {
  id: number;
  name: string;
  chemicalFormula: string | null;
  cas: string;
  type: IngredientType;
  status: EntityStatus;
}

export interface IngredientRequest {
  name: string;
  chemicalFormula: string;
  cas: string;
  type: IngredientType;
}

export interface ProductResponse {
  id: number;
  name: string;
  description: string | null;
  subcategoryId: number;
  subcategoryName: string;
  therapeuticAreaId: number;
  therapeuticAreaName: string;
  status: EntityStatus;
}

export interface ProductRequest {
  name: string;
  description: string;
  subcategoryId: number;
  therapeuticAreaId: number;
}

export interface VariantResponse {
  id: number;
  productId: number;
  productName: string;
  form: string;
  dosage: string;
  status: EntityStatus;
}

export interface VariantRequest {
  productId: number;
  form: string;
  dosage: string;
}

export interface VariantVersionResponse {
  id: number;
  variantId: number;
  productName: string;
  variantForm: string;
  variantDosage: string;
  versionLabel: string;
  description: string | null;
  status: VariantVersionStatus;
}

export interface VariantVersionRequest {
  variantId: number;
  versionLabel: string;
  description: string;
}

export interface VariantVersionStatusRequest {
  status: VariantVersionStatus;
}

export interface VariantVersionIngredientsResponse {
  id: number;
  variantVersionId: number;
  versionLabel: string;
  variantId: number;
  productName: string;
  variantForm: string;
  variantDosage: string;
  ingredientId: number;
  ingredientName: string;
  ingredientCas: string;
  ingredientChemicalFormula: string | null;
  amount: number;
  unit: string;
}

export interface VariantVersionIngredientsRequest {
  variantVersionId: number;
  ingredientId: number;
  amount: number;
  unit: string;
}

//sprint2

export type MarketLicenseStatus =
  | 'SUBMITTED'
  | 'APPROVED'
  | 'RENEWAL_IN_PROGRESS'
  | 'EXPIRED'
  | 'SUSPENDED';

export interface MarketProductResponse {
  id: number;
  variantId: number;
  productName: string;
  variantForm: string;
  variantDosage: string;
  regionId: number;
  regionName: string;
  regionCode: string;
  localName: string;
  packagingDescription: string | null;
  barcode: string | null;
  status: EntityStatus;
}

export interface MarketProductRequest {
  variantId: number;
  regionId: number;
  localName: string;
  packagingDescription: string;
  barcode: string;
}

export interface MarketLicenseResponse {
  id: number;
  marketProductId: number;
  localName: string;
  regionName: string;
  regionCode: string;
  variantVersionId: number;
  productName: string;
  variantForm: string;
  variantDosage: string;
  versionLabel: string;
  licenseNumber: string;
  issuedAt: string | null;
  validUntil: string | null;
  status: MarketLicenseStatus;
}

export interface MarketLicenseRequest {
  marketProductId: number;
  variantVersionId: number;
  licenseNumber: string;
  issuedAt: string | null;
  validUntil: string | null;
}

export interface MarketLicenseStatusRequest {
  status: MarketLicenseStatus;
}

export interface MarketLicenseHistoryResponse {
  id: number;
  marketLicenseId: number;
  licenseNumber: string;
  localName: string;
  regionName: string;
  productName: string;
  versionLabel: string;
  oldStatus: MarketLicenseStatus | null;
  newStatus: MarketLicenseStatus;
  changedAt: string;
  changedBy: number | null;
  note: string | null;
}

export interface VariantVersionLifecycleHistoryResponse {
  id: number;
  variantVersionId: number;
  productName: string;
  variantForm: string;
  variantDosage: string;
  versionLabel: string;
  oldStatus: VariantVersionStatus | null;
  newStatus: VariantVersionStatus;
  changedAt: string;
  changedBy: number | null;
  reason: string | null;
  automaticTransition: boolean;
}

export interface VariantVersionStatusCountResponse {
  status: VariantVersionStatus;
  count: number;
}

export interface ProductCountByTherapeuticAreaResponse {
  therapeuticAreaId: number;
  therapeuticAreaName: string;
  productCount: number;
}

export interface RegionResponse {
  id: number;
  name: string;
  code: string;
}

export interface MarketLicenseResponse {
  id: number;

  marketProductId: number;
  productName: string;
  variantForm: string;
  variantDosage: string;
  regionName: string;
  variantVersionId: number;
  versionLabel: string;
  licenseNumber: string;
  issuedAt: string | null;
  validUntil: string | null;
  status: MarketLicenseStatus;
}

export interface MarketLicenseRequest {
  marketProductId: number;
  variantVersionId: number;
  licenseNumber: string;
  issuedAt: string | null;
  validUntil: string | null;
}

export interface MarketLicenseStatusRequest {
  status: MarketLicenseStatus;
}