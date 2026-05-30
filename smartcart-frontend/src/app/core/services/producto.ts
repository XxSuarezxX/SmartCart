import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private apiUrl = '';

  constructor(private http: HttpClient, private authService: AuthService) { }

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.authService.getToken()}`
    });
  }

  getProductos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/productos`);
  }

  getCategorias(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/productos/categorias`);
  }

  getProductoPorId(id: number): Observable<any> {
    return this.getProductos().pipe(
      map((productos: any[]) => productos.find(p => p.id === id))
    );
  }

  crearProducto(producto: any): Observable<any> {
  return this.http.post(`${this.apiUrl}/productos`, producto, {
    headers: this.getHeaders()
  });
}

editarProducto(id: number, producto: any): Observable<any> {
  return this.http.put(`${this.apiUrl}/productos/${id}`, producto, {
    headers: this.getHeaders()
  });
}

eliminarProducto(id: number): Observable<any> {
  return this.http.delete(`${this.apiUrl}/productos/${id}`, {
    headers: this.getHeaders()
  });
}

crearCategoria(categoria: any): Observable<any> {
  return this.http.post(`${this.apiUrl}/productos/categorias`, categoria, {
    headers: this.getHeaders()
  });
}

eliminarCategoria(id: number): Observable<any> {
  return this.http.delete(`${this.apiUrl}/productos/categorias/${id}`, {
    headers: this.getHeaders()
  });
}
}