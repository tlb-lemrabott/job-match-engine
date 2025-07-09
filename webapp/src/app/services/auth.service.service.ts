import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

export interface LoginResponse {
  success: boolean;
  message: string;
  token?: string;
  user?: {
    id: string;
    email: string;
    name: string;
  };
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  name: string;
  email: string;
  password: string;
  phone: string;
}

export interface SignupResponse {
  success: boolean;
  message: string;
  user?: {
    id: string;
    email: string;
    name: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthServiceService {
  private apiUrl = 'http://localhost:8080';
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.isAuthenticated());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient) { }

  login(email: string, password: string): Observable<LoginResponse> {
    const loginData: LoginRequest = { email, password };
    
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    };

    return this.http.post<LoginResponse>(`${this.apiUrl}/auth/login`, loginData, httpOptions)
      .pipe(
        map(response => {
          // Store token in localStorage if login is successful
          if (response.success && response.token) {
            localStorage.setItem('authToken', response.token);
            if (response.user) {
              localStorage.setItem('user', JSON.stringify(response.user));
            }
            this.isAuthenticatedSubject.next(true);
          }
          return response;
        }),
        catchError(error => {
          console.error('Login error:', error);
          return throwError(() => new Error(error.error?.message || 'Login failed. Please try again.'));
        })
      );
  }

  signup(name: string, email: string, password: string, phone: string): Observable<SignupResponse> {
    const signupData: SignupRequest = { name, email, password, phone };
    
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    };

    return this.http.post<SignupResponse>(`${this.apiUrl}/auth/signup`, signupData, httpOptions)
      .pipe(
        map(response => {
          // Store user data if signup is successful
          if (response.success && response.user) {
            localStorage.setItem('user', JSON.stringify(response.user));
          }
          return response;
        }),
        catchError(error => {
          console.error('Signup error:', error);
          return throwError(() => new Error(error.error?.message || 'Signup failed. Please try again.'));
        })
      );
  }

  logout(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
    this.isAuthenticatedSubject.next(false);
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('authToken');
  }

  getToken(): string | null {
    return localStorage.getItem('authToken');
  }

  getUser(): any {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }

  // Method to refresh authentication status
  refreshAuthStatus(): void {
    this.isAuthenticatedSubject.next(this.isAuthenticated());
  }

  // Method to check if token is expired (basic implementation)
  isTokenExpired(): boolean {
    const token = this.getToken();
    if (!token) return true;
    
    try {
      // Basic JWT token expiration check
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Date.now() / 1000;
      return payload.exp < currentTime;
    } catch (error) {
      console.error('Error parsing token:', error);
      return true;
    }
  }
}
