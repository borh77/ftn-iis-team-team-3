import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.token';
import { Category, Product, Subcategory, Variant } from './portfolio.models';

@Injectable({ providedIn: 'root' })
export class PortfolioService {
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly http = inject(HttpClient);

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiBaseUrl}/api/categories`);
  }

  getSubcategories(categoryId?: number): Observable<Subcategory[]> {
    const params = categoryId == null ? undefined : new HttpParams().set('categoryId', categoryId);
    return this.http.get<Subcategory[]>(`${this.apiBaseUrl}/api/subcategories`, { params });
  }

  getProducts(subcategoryId?: number): Observable<Product[]> {
    const params = subcategoryId == null ? undefined : new HttpParams().set('subcategoryId', subcategoryId);
    return this.http.get<Product[]>(`${this.apiBaseUrl}/api/products`, { params });
  }

  getVariants(productId?: number): Observable<Variant[]> {
    const params = productId == null ? undefined : new HttpParams().set('productId', productId);
    return this.http.get<Variant[]>(`${this.apiBaseUrl}/api/variants`, { params });
  }
}