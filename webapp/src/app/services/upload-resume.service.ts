import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpEventType } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class UploadResumeService {
  private uploadUrl = '/api/resume/upload';

  constructor(private http: HttpClient) {}

  uploadResume(file: File): Observable<HttpEvent<any>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(this.uploadUrl, formData, {
      reportProgress: true,
      observe: 'events'
    });
  }
}
