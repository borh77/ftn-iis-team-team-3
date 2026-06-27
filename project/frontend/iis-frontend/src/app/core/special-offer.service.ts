import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.token';
import { CreateSpecialOfferPayload, PromotionSuggestion, SpecialOffer } from './special-offer.models';

@Injectable({ providedIn: 'root' })
export class SpecialOfferService {
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly http = inject(HttpClient);

  create(payload: CreateSpecialOfferPayload): Observable<SpecialOffer> {
    return this.http.post<SpecialOffer>(`${this.apiBaseUrl}/api/special-offers`, payload);
  }

  listForPricelist(pricelistId: number): Observable<SpecialOffer[]> {
    return this.http.get<SpecialOffer[]>(`${this.apiBaseUrl}/api/special-offers/pricelist/${pricelistId}`);
  }

  activate(id: number): Observable<SpecialOffer> {
    return this.http.put<SpecialOffer>(`${this.apiBaseUrl}/api/special-offers/${id}/activate`, {});
  }

  archive(id: number): Observable<SpecialOffer> {
    return this.http.put<SpecialOffer>(`${this.apiBaseUrl}/api/special-offers/${id}/archive`, {});
  }

  getPromotionSuggestions(segment: string): Observable<PromotionSuggestion[]> {
    return this.http.get<PromotionSuggestion[]>(`${this.apiBaseUrl}/api/promotions/suggestions`, {
      params: { segment },
    });
  }
}
