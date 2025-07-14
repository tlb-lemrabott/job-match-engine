import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap, map } from 'rxjs/operators';

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

export interface ResumeResponse {
  success: boolean;
  message: string;
  data?: Resume;
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

    console.log('Uploading file:', {
      name: file.name,
      size: file.size,
      type: file.type
    });

    const httpOptions = {
      headers: new HttpHeaders({
        'Authorization': `Bearer ${this.getAuthToken()}`
      })
    };

    return this.http.post<ResumeUploadResponse>(`${this.apiUrl}/resume/upload`, formData, httpOptions)
      .pipe(
        tap(response => {
          console.log('Raw API response from uploadResume:', response);
          console.log('Upload response JSON:', JSON.stringify(response, null, 2));
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

    return this.http.get<ResumeResponse | Resume>(`${this.apiUrl}/resume/user`, httpOptions)
      .pipe(
        tap(response => {
          console.log('Raw API response from getUserResume:', response);
        }),
        map(response => {
          // Handle both direct Resume object and wrapped response
          if (response && typeof response === 'object' && 'data' in response) {
            const resumeData = (response as ResumeResponse).data;
            if (!resumeData) {
              throw new Error('No resume data found in response');
            }
            return resumeData;
          }
          return response as Resume;
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