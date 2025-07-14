import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

export interface Resume {
  id: string;
  fileName: string;
  fileSize: number;
  uploadDate: Date;
  fileType: string;
  fileUrl?: string;
}

export interface ResumeUploadResponse {
  success: boolean;
  message: string;
  resume?: Resume;
}

export interface ResumeDeleteResponse {
  success: boolean;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class ResumeService {
  private apiUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) { }

  uploadResume(file: File): Observable<ResumeUploadResponse> {
    const formData = new FormData();
    formData.append('resume', file);

    const httpOptions = {
      headers: new HttpHeaders({
        'Authorization': `Bearer ${this.getAuthToken()}`
      })
    };

    return this.http.post<ResumeUploadResponse>(`${this.apiUrl}/resume/upload`, formData, httpOptions)
      .pipe(
        tap(response => {
          console.log('Raw API response from uploadResume:', response);
        }),
        catchError(error => {
          console.error('Resume upload error:', error);
          return throwError(() => new Error(error.error?.message || 'Resume upload failed. Please try again.'));
        })
      );
  }

  getUserResume(): Observable<Resume> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Authorization': `Bearer ${this.getAuthToken()}`
      })
    };

    return this.http.get<Resume>(`${this.apiUrl}/resume/user`, httpOptions)
      .pipe(
        tap(response => {
          console.log('Raw API response from getUserResume:', response);
        }),
        catchError(error => {
          console.error('Get resume error:', error);
          return throwError(() => new Error(error.error?.message || 'Failed to fetch resume.'));
        })
      );
  }

  deleteResume(): Observable<ResumeDeleteResponse> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Authorization': `Bearer ${this.getAuthToken()}`
      })
    };

    return this.http.delete<ResumeDeleteResponse>(`${this.apiUrl}/resume/user`, httpOptions)
      .pipe(
        catchError(error => {
          console.error('Delete resume error:', error);
          return throwError(() => new Error(error.error?.message || 'Failed to delete resume.'));
        })
      );
  }

  private getAuthToken(): string | null {
    return localStorage.getItem('authToken');
  }
} 