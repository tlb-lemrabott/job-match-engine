import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface JobMatchingRequest {
  resume: string; // UUID as string
  type: 'full-job' | 'skills-section';
  textArea: string;
}

export interface MatchedSkill {
  skill: string;
  confidence: number;
  category?: string;
}

export interface MissingSkill {
  skill: string;
  importance: number;
  category?: string;
}

export interface JobMatchingResponse {
  success: boolean;
  message: string;
  matchingScore: number;
  matchedSkills: MatchedSkill[];
  missingSkills: MissingSkill[];
  analysis?: {
    overallMatch: string;
    recommendations: string[];
  };
}

@Injectable({
  providedIn: 'root'
})
export class JobMatchingService {
  private apiUrl = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient) { }

  checkJobMatch(request: JobMatchingRequest): Observable<JobMatchingResponse> {
    const token = this.getAuthToken();
    console.log('Auth token:', token ? 'Present' : 'Missing');
    
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      })
    };

    console.log('Making request to:', `${this.apiUrl}/job-matching`);
    console.log('Request payload:', request);

    return this.http.post<JobMatchingResponse>(`${this.apiUrl}/job-matching`, request, httpOptions)
      .pipe(
        catchError(error => {
          console.error('Job matching error:', error);
          console.error('Error details:', error.error);
          return throwError(() => new Error(error.error?.message || 'Job matching failed. Please try again.'));
        })
      );
  }

  private getAuthToken(): string | null {
    return localStorage.getItem('authToken');
  }
} 