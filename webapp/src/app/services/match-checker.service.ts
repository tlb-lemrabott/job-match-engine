import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class MatchCheckerService {
  private apiUrl = '/api/match-check';

  constructor(private http: HttpClient) { }

  checkMatch(data: { jobDescription: string; skills: string }): Observable<{ matchPercentage: number }> {
    return this.http.post<{ matchPercentage: number }>(this.apiUrl, data);
  }
  
}
