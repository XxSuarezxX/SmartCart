import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class EstadoService {
  private favoritosSubject = new BehaviorSubject<any[]>(this.getFavoritos());
  private carritoSubject = new BehaviorSubject<any[]>(this.getCarrito());

  favoritos$ = this.favoritosSubject.asObservable();
  carrito$ = this.carritoSubject.asObservable();

  private getFavoritos(): any[] {
    return JSON.parse(localStorage.getItem('favoritos') || '[]');
  }

  private getCarrito(): any[] {
    return JSON.parse(localStorage.getItem('carrito') || '[]');
  }

  actualizarFavoritos(favoritos: any[]) {
    localStorage.setItem('favoritos', JSON.stringify(favoritos));
    this.favoritosSubject.next(favoritos);
  }

  actualizarCarrito(carrito: any[]) {
    localStorage.setItem('carrito', JSON.stringify(carrito));
    this.carritoSubject.next(carrito);
  }

  agregarFavorito(producto: any) {
    const favoritos = this.getFavoritos();
    if (!favoritos.find((p: any) => p.id === producto.id)) {
      favoritos.push(producto);
      this.actualizarFavoritos(favoritos);
    }
  }

  quitarFavorito(productoId: number) {
    const favoritos = this.getFavoritos().filter((p: any) => p.id !== productoId);
    this.actualizarFavoritos(favoritos);
  }

  refrescarCarrito() {
    this.carritoSubject.next(this.getCarrito());
  }

  get contadorFavoritos(): number {
    return this.getFavoritos().length;
  }

  get contadorCarrito(): number {
    return this.getCarrito().reduce((acc: number, item: any) => acc + (item.cantidad || 1), 0);
  }
}