import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { API_BASE_URL } from '../api.token';
import {
  CategoryRequest,
  CategoryResponse,
  IngredientRequest,
  IngredientResponse,
  ProductRequest,
  ProductResponse,
  SubcategoryRequest,
  SubcategoryResponse,
  TherapeuticAreaRequest,
  TherapeuticAreaResponse,
  VariantRequest,
  VariantResponse,
  VariantVersionIngredientsRequest,
  VariantVersionIngredientsResponse,
  VariantVersionRequest,
  VariantVersionResponse,
  VariantVersionStatus,
  VariantVersionStatusRequest,
  MarketProductRequest,
  MarketProductResponse,
  MarketLicenseRequest,
  MarketLicenseResponse,
  MarketLicenseStatus,
  MarketLicenseStatusRequest,
  MarketLicenseHistoryResponse,
  VariantVersionLifecycleHistoryResponse,
  VariantVersionStatusCountResponse,
  ProductCountByTherapeuticAreaResponse,
  RegionResponse,
  MarketLicenseStatusCountResponse,
  MarketProductCountByRegionResponse,
} from './portfolio.models';

@Injectable({ providedIn: 'root' })
export class PortfolioService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getCategories() {
    return this.http.get<CategoryResponse[]>(`${this.apiBaseUrl}/api/categories`);
  }

  createCategory(payload: CategoryRequest) {
    return this.http.post<CategoryResponse>(`${this.apiBaseUrl}/api/categories`, payload);
  }

  updateCategory(id: number, payload: CategoryRequest) {
    return this.http.put<CategoryResponse>(`${this.apiBaseUrl}/api/categories/${id}`, payload);
  }

  getSubcategories(categoryId?: number) {
    let params = new HttpParams();

    if (categoryId) {
      params = params.set('categoryId', categoryId);
    }

    return this.http.get<SubcategoryResponse[]>(`${this.apiBaseUrl}/api/subcategories`, { params });
  }

  createSubcategory(payload: SubcategoryRequest) {
    return this.http.post<SubcategoryResponse>(`${this.apiBaseUrl}/api/subcategories`, payload);
  }

  updateSubcategory(id: number, payload: SubcategoryRequest) {
    return this.http.put<SubcategoryResponse>(`${this.apiBaseUrl}/api/subcategories/${id}`, payload);
  }

  getTherapeuticAreas() {
    return this.http.get<TherapeuticAreaResponse[]>(`${this.apiBaseUrl}/api/therapeutic-areas`);
  }

  createTherapeuticArea(payload: TherapeuticAreaRequest) {
    return this.http.post<TherapeuticAreaResponse>(`${this.apiBaseUrl}/api/therapeutic-areas`, payload);
  }

  updateTherapeuticArea(id: number, payload: TherapeuticAreaRequest) {
    return this.http.put<TherapeuticAreaResponse>(`${this.apiBaseUrl}/api/therapeutic-areas/${id}`, payload);
  }

  getIngredients(search?: string) {
    let params = new HttpParams();

    if (search?.trim()) {
      params = params.set('search', search.trim());
    }

    return this.http.get<IngredientResponse[]>(`${this.apiBaseUrl}/api/ingredients`, { params });
  }

  createIngredient(payload: IngredientRequest) {
    return this.http.post<IngredientResponse>(`${this.apiBaseUrl}/api/ingredients`, payload);
  }

  updateIngredient(id: number, payload: IngredientRequest) {
    return this.http.put<IngredientResponse>(`${this.apiBaseUrl}/api/ingredients/${id}`, payload);
  }

  getProducts(search?: string, subcategoryId?: number, therapeuticAreaId?: number, includeArchived = false) {
    let params = new HttpParams().set('includeArchived', includeArchived);

    if (search?.trim()) {
      params = params.set('search', search.trim());
    }

    if (subcategoryId) {
      params = params.set('subcategoryId', subcategoryId);
    }

    if (therapeuticAreaId) {
      params = params.set('therapeuticAreaId', therapeuticAreaId);
    }

    return this.http.get<ProductResponse[]>(`${this.apiBaseUrl}/api/products`, { params });
  }

  createProduct(payload: ProductRequest) {
    return this.http.post<ProductResponse>(`${this.apiBaseUrl}/api/products`, payload);
  }

  updateProduct(id: number, payload: ProductRequest) {
    return this.http.put<ProductResponse>(`${this.apiBaseUrl}/api/products/${id}`, payload);
  }

  archiveProduct(id: number) {
    return this.http.patch<void>(`${this.apiBaseUrl}/api/products/${id}/archive`, {});
  }

  getVariants(search?: string, productId?: number, includeArchived = false) {
    let params = new HttpParams().set('includeArchived', includeArchived);

    if (search?.trim()) {
      params = params.set('search', search.trim());
    }

    if (productId) {
      params = params.set('productId', productId);
    }

    return this.http.get<VariantResponse[]>(`${this.apiBaseUrl}/api/variants`, { params });
  }

  createVariant(payload: VariantRequest) {
    return this.http.post<VariantResponse>(`${this.apiBaseUrl}/api/variants`, payload);
  }

  updateVariant(id: number, payload: VariantRequest) {
    return this.http.put<VariantResponse>(`${this.apiBaseUrl}/api/variants/${id}`, payload);
  }

  archiveVariant(id: number) {
    return this.http.patch<void>(`${this.apiBaseUrl}/api/variants/${id}/archive`, {});
  }

  getVariantVersions(search?: string, variantId?: number, status?: VariantVersionStatus) {
    let params = new HttpParams();

    if (search?.trim()) {
      params = params.set('search', search.trim());
    }

    if (variantId) {
      params = params.set('variantId', variantId);
    }

    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<VariantVersionResponse[]>(`${this.apiBaseUrl}/api/variant-versions`, { params });
  }

  createVariantVersion(payload: VariantVersionRequest) {
    return this.http.post<VariantVersionResponse>(`${this.apiBaseUrl}/api/variant-versions`, payload);
  }

  updateVariantVersion(id: number, payload: VariantVersionRequest) {
    return this.http.put<VariantVersionResponse>(`${this.apiBaseUrl}/api/variant-versions/${id}`, payload);
  }

  changeVariantVersionStatus(id: number, payload: VariantVersionStatusRequest) {
    return this.http.patch<VariantVersionResponse>(`${this.apiBaseUrl}/api/variant-versions/${id}/status`, payload);
  }

  getVersionIngredients(variantVersionId?: number) {
    let params = new HttpParams();

    if (variantVersionId) {
      params = params.set('variantVersionId', variantVersionId);
    }

    return this.http.get<VariantVersionIngredientsResponse[]>(
      `${this.apiBaseUrl}/api/variant-version-ingredients`,
      { params },
    );
  }

  createVersionIngredient(payload: VariantVersionIngredientsRequest) {
    return this.http.post<VariantVersionIngredientsResponse>(
      `${this.apiBaseUrl}/api/variant-version-ingredients`,
      payload,
    );
  }

  updateVersionIngredient(id: number, payload: VariantVersionIngredientsRequest) {
    return this.http.put<VariantVersionIngredientsResponse>(
      `${this.apiBaseUrl}/api/variant-version-ingredients/${id}`,
      payload,
    );
  }
//sprint2
  getMarketProducts(search?: string, variantId?: number, regionId?: number, includeArchived = false) {
    let params = new HttpParams().set('includeArchived', includeArchived);

    if (search?.trim()) {
      params = params.set('search', search.trim());
    }

    if (variantId) {
      params = params.set('variantId', variantId);
    }

    if (regionId) {
      params = params.set('regionId', regionId);
    }

    return this.http.get<MarketProductResponse[]>(
      `${this.apiBaseUrl}/api/market-products`,
      { params },
    );
  }

  createMarketProduct(payload: MarketProductRequest) {
    return this.http.post<MarketProductResponse>(
      `${this.apiBaseUrl}/api/market-products`,
      payload,
    );
  }

  updateMarketProduct(id: number, payload: MarketProductRequest) {
    return this.http.put<MarketProductResponse>(
      `${this.apiBaseUrl}/api/market-products/${id}`,
      payload,
    );
  }

  archiveMarketProduct(id: number) {
    return this.http.patch<void>(
      `${this.apiBaseUrl}/api/market-products/${id}/archive`,
      {},
    );
  }

  getMarketLicenses(
    search?: string,
    marketProductId?: number,
    variantVersionId?: number,
    status?: MarketLicenseStatus,
  ) {
    let params = new HttpParams();

    if (search?.trim()) {
      params = params.set('search', search.trim());
    }

    if (marketProductId) {
      params = params.set('marketProductId', marketProductId);
    }

    if (variantVersionId) {
      params = params.set('variantVersionId', variantVersionId);
    }

    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<MarketLicenseResponse[]>(
      `${this.apiBaseUrl}/api/market-licenses`,
      { params },
    );
  }

  createMarketLicense(payload: MarketLicenseRequest) {
    return this.http.post<MarketLicenseResponse>(
      `${this.apiBaseUrl}/api/market-licenses`,
      payload,
    );
  }

  updateMarketLicense(id: number, payload: MarketLicenseRequest) {
    return this.http.put<MarketLicenseResponse>(
      `${this.apiBaseUrl}/api/market-licenses/${id}`,
      payload,
    );
  }

  changeMarketLicenseStatus(id: number, payload: MarketLicenseStatusRequest) {
    return this.http.patch<MarketLicenseResponse>(
      `${this.apiBaseUrl}/api/market-licenses/${id}/status`,
      payload,
    );
  }

  getMarketLicenseHistory(id: number) {
    return this.http.get<MarketLicenseHistoryResponse[]>(
      `${this.apiBaseUrl}/api/market-licenses/${id}/history`,
    );
  }

  getLicensesExpiringUntil(date: string) {
    return this.http.get<MarketLicenseResponse[]>(
      `${this.apiBaseUrl}/api/market-licenses/expiring`,
      { params: new HttpParams().set('date', date) },
    );
  }

  getVariantVersionHistory(id: number) {
    return this.http.get<VariantVersionLifecycleHistoryResponse[]>(
      `${this.apiBaseUrl}/api/variant-versions/${id}/history`,
    );
  }

  getVariantLifecycleHistory(variantId: number) {
    return this.http.get<VariantVersionLifecycleHistoryResponse[]>(
      `${this.apiBaseUrl}/api/portfolio-analytics/variants/${variantId}/lifecycle-history`,
    );
  }

  getVariantVersionStatusCount() {
    return this.http.get<VariantVersionStatusCountResponse[]>(
      `${this.apiBaseUrl}/api/portfolio-analytics/variant-version-status-count`,
    );
  }

  getProductsByTherapeuticArea() {
    return this.http.get<ProductCountByTherapeuticAreaResponse[]>(
      `${this.apiBaseUrl}/api/portfolio-analytics/products/by-therapeutic-area`,
    );
  }

  getRegions() {
    return this.http.get<RegionResponse[]>(`${this.apiBaseUrl}/api/regions`);
  }

  getMarketLicenseStatusCount() {
    return this.http.get<MarketLicenseStatusCountResponse[]>(
      `${this.apiBaseUrl}/api/portfolio-analytics/market-license-status-count`,
    );
  }

  getMarketProductsByRegion() {
    return this.http.get<MarketProductCountByRegionResponse[]>(
      `${this.apiBaseUrl}/api/portfolio-analytics/market-products/by-region`,
    );
  }
  
}