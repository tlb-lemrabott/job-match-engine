import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SignupPayload {
  name: string;
  email: string;
  password: string;
}

@Injectable({
  providedIn: 'root'
})

export class SignupService {
  private apiUrl = '/api/auth/signup';

  constructor(private http: HttpClient) {}

  signup(payload: SignupPayload): Observable<any> {
    return this.http.post(this.apiUrl, payload);
  }

}