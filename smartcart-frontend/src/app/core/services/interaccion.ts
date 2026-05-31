import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth';

@Injectable({ providedIn: 'root' })
export class InteraccionService {
  // 1. Apuntamos directo al puerto de tu Spring Boot
  private apiUrl = 'http://localhost:8080';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.authService.getToken()}`
    });
  }

  // 2. Agregamos 'tipo' para que tu backend sepa si es VER, AGREGAR o COMPRA
  registrarInteraccion(usuarioId: number, categoriaId: number, tipo: string) {
      // Declaramos las llaves del JSON explícitamente para que coincidan con tu Java
      const body = {
        usuarioId: usuarioId,
        categoriaId: categoriaId,
        tipo: tipo
      };

      return this.http.post(`${this.apiUrl}/api/interacciones/registrar`,
        body,
        { headers: this.getHeaders() }
      );
    }

  // 3. Retorna un Observable con la lista de productos recomendados
  getSugeridos(usuarioId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/api/interacciones/sugeridos/${usuarioId}`,
      { headers: this.getHeaders() }
    );
  }
}
