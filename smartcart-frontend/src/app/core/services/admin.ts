import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = '/api/admin';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` });
  }

  getDashboard(): Observable<any> {
    return this.http.get(`${this.apiUrl}/dashboard`, { headers: this.headers() });
  }
}