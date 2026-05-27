import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.token';
import { CreateTeamPayload, PricelistTeam, TeamMember, TeamMemberRequestPayload } from './team.models';

@Injectable({ providedIn: 'root' })
export class TeamService {
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly http = inject(HttpClient);

  getMyTeams(): Observable<PricelistTeam[]> {
    return this.http.get<PricelistTeam[]>(`${this.apiBaseUrl}/api/teams/me`);
  }

  createTeam(payload: CreateTeamPayload): Observable<PricelistTeam> {
    return this.http.post<PricelistTeam>(`${this.apiBaseUrl}/api/teams`, payload);
  }

  addMember(teamId: number, payload: TeamMemberRequestPayload): Observable<PricelistTeam> {
    return this.http.put<PricelistTeam>(`${this.apiBaseUrl}/api/teams/${teamId}/members/add`, payload);
  }

  removeMember(teamId: number, payload: TeamMemberRequestPayload): Observable<PricelistTeam> {
    return this.http.put<PricelistTeam>(`${this.apiBaseUrl}/api/teams/${teamId}/members/remove`, payload);
  }

  searchUsers(username: string): Observable<TeamMember[]> {
    const params = new HttpParams().set('username', username);
    return this.http.get<TeamMember[]>(`${this.apiBaseUrl}/api/users/search`, { params });
  }
}