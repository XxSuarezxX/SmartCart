import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';


@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) { }

  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/api/auth/login`, { email, password });
  }

  registro(nombre: string, email: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/api/auth/registro`, { nombre, email, password });
  }

  guardarSesion(token: string, username: string, rol: string, userId?: number) {
    localStorage.setItem('token', token);
    localStorage.setItem('username', username);
    localStorage.setItem('rol', rol);
    if (userId) localStorage.setItem('userId', userId.toString());
  }

  getUserId(): number {
    return parseInt(localStorage.getItem('userId') || '0');
  }
  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getRol(): string | null {
    return localStorage.getItem('rol');
  }

  getUsername(): string | null {
    return localStorage.getItem('username');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    return this.getRol() === 'ADMIN';
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('rol');
  }

  getUsuarios(): Observable<any[]> {
  return this.http.get<any[]>(`${this.apiUrl}/api/auth/usuarios`, {
    headers: this.getHeaders()
  });
}

private getHeaders(): HttpHeaders {
  return new HttpHeaders({
    Authorization: `Bearer ${this.getToken()}`
  });
}
}
