import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from './auth';

@Injectable({ providedIn: 'root' })
export class InteraccionService {
  private apiUrl = 'http://localhost:8080';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.authService.getToken()}`
    });
  }

  registrarInteraccion(usuarioId: number, productoId: number, categoriaId: number) {
    return this.http.post(`${this.apiUrl}/api/interacciones/registrar`,
      { usuarioId, productoId, categoriaId },
      { headers: this.getHeaders() }
    );
  }

  getSugeridos(usuarioId: number) {
    return this.http.get<any[]>(`${this.apiUrl}/api/interacciones/sugeridos/${usuarioId}`,
      { headers: this.getHeaders() }
    );
  }
}