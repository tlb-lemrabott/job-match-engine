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
  resume?: Resume;
}

export interface ResumeDeleteResponse {
  success: boolean;
  message: string;
}

// Backend response structure
export interface BackendResumeResponse {
  success: boolean;
  message: string;
  resume?: {
    id: string;
    fileName: string;
    fileSize: number;
    uploadDate: string; // Java LocalDateTime comes as string
    fileType: string;
    fileUrl?: string;
  };
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

    return this.http.post<BackendResumeResponse>(`${this.apiUrl}/resume/upload`, formData, httpOptions)
      .pipe(
        map(response => {
          if (response.success && response.resume) {
            return {
              success: true,
              message: response.message,
              resume: this.mapBackendResumeToFrontend(response.resume)
            };
          } else {
            return {
              success: false,
              message: response.message,
              resume: undefined
            };
          }
        }),
        catchError(error => {
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

    return this.http.get<BackendResumeResponse>(`${this.apiUrl}/resume/user`, httpOptions)
      .pipe(
        map(response => {
          if (response.success && response.resume) {
            return this.mapBackendResumeToFrontend(response.resume);
          } else {
            throw new Error(response.message || 'No resume found');
          }
        }),
        catchError(error => {
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
        tap(response => {
          console.log('Delete resume response:', response);
        }),
        catchError(error => {
          console.error('Delete resume error:', error);
          
          // Handle specific Hibernate/JPA errors
          if (error.error?.message?.includes('TransientObjectException')) {
            return throwError(() => new Error('Resume record is in an inconsistent state. Please try uploading a new resume.'));
          }
          
          // Handle other common errors
          if (error.status === 404) {
            return throwError(() => new Error('Resume not found. It may have already been deleted.'));
          }
          
          if (error.status === 403) {
            return throwError(() => new Error('You do not have permission to delete this resume.'));
          }
          
          return throwError(() => new Error(error.error?.message || 'Failed to delete resume. Please try again.'));
        })
      );
  }

  private mapBackendResumeToFrontend(backendResume: any): Resume {
    return {
      id: backendResume.id || '',
      fileName: backendResume.fileName || 'Unknown File',
      fileSize: this.parseFileSize(backendResume.fileSize),
      fileType: backendResume.fileType || 'application/octet-stream',
      uploadDate: this.parseUploadDate(backendResume.uploadDate),
      fileUrl: backendResume.fileUrl
    };
  }

  private parseFileSize(size: any): number {
    if (typeof size === 'number') {
      return size;
    }
    if (typeof size === 'string') {
      const parsed = parseInt(size, 10);
      return isNaN(parsed) ? 0 : parsed;
    }
    return 0;
  }

  private parseUploadDate(dateString: any): Date {
    if (!dateString) {
      return new Date();
    }
    
    if (dateString instanceof Date) {
      return dateString;
    }
    
    if (typeof dateString === 'string') {
      try {
        return new Date(dateString);
      } catch (error) {
        return new Date();
      }
    }
    
    if (typeof dateString === 'object' && dateString !== null) {
      try {
        if (dateString.year && dateString.monthValue && dateString.dayOfMonth) {
          return new Date(
            dateString.year,
            dateString.monthValue - 1,
            dateString.dayOfMonth,
            dateString.hour || 0,
            dateString.minute || 0,
            dateString.second || 0
          );
        }
      } catch (error) {
        // Fallback to current date
      }
    }
    
    return new Date();
  }

  private getAuthToken(): string | null {
    return localStorage.getItem('authToken');
  }
} 