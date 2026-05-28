import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth';

@Injectable({ providedIn: 'root' })
export class CarritoService {
  private apiUrl = 'http://localhost:8080';

  constructor(private http: HttpClient, private authService: AuthService) { }

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.authService.getToken()}`
    });
  }

  getCarrito(usuarioId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/carrito/${usuarioId}`, {
      headers: this.getHeaders()
    });
  }

  agregarProducto(usuarioId: number, productoId: number, cantidad: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/carrito/${usuarioId}/agregar`,
      { productoId, cantidad },
      { headers: this.getHeaders() }
    );
  }

  actualizarCantidad(carritoId: number, cantidad: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/carrito/item/${carritoId}`,
      { cantidad },
      { headers: this.getHeaders() }
    );
  }

  eliminarItem(carritoId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/carrito/item/${carritoId}`, {
      headers: this.getHeaders()
    });
  }

  vaciarCarrito(usuarioId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/carrito/${usuarioId}/vaciar`, {
      headers: this.getHeaders()
    });
  }

  procesarPago(pago: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/api/pagos/procesar`, pago, {
      headers: this.getHeaders()
    });
  }

  getHistorial(usuarioId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/api/pagos/historial/${usuarioId}`, {
      headers: this.getHeaders()
    });
  }
}