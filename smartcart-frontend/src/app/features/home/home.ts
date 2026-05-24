import { Component, OnInit, ChangeDetectorRef, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductoService } from '../../core/services/producto';
import { InteraccionService } from '../../core/services/interaccion';
import { AuthService } from '../../core/services/auth';
import { Router } from '@angular/router';
import { PrecioPipe } from '../../shared/pipes/precio-pipe';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, PrecioPipe],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent implements OnInit {
  productos: any[] = [];
  productosFiltrados: any[] = [];
  private _categoria = '';

  @Input() set categoria(valor: string) {
    this._categoria = valor;
    this.filtrar();
  }

  constructor(
    private productoService: ProductoService,
    private interaccionService: InteraccionService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) { }

  ngOnInit() {
    this.productoService.getProductos().subscribe(data => {
      this.productos = data.map(p => ({ ...p, liked: false }));
      this.filtrar();
      this.cdr.detectChanges();
    });
  }

  filtrar() {
    if (!this._categoria) {
      this.productosFiltrados = this.productos;
    } else {
      this.productosFiltrados = this.productos.filter(
        p => p.categoria?.nombre === this._categoria
      );
    }
    this.cdr.detectChanges();
  }

  toggleLike(event: Event, producto: any) {
    event.stopPropagation();
    if (!this.authService.isLoggedIn()) return;

    producto.liked = !producto.liked;

    if (producto.liked) {
      const usuarioId = this.authService.getUserId();
      this.interaccionService.registrarInteraccion(
        usuarioId, producto.id, producto.categoria?.id
      ).subscribe();
    }
    this.cdr.detectChanges();
  }

  irADetalle(id: number) {
    this.router.navigate(['/producto', id]);
  }

  agregarCarrito(producto: any) {
    const carrito = JSON.parse(localStorage.getItem('carrito') || '[]');
    const existe = carrito.find((p: any) => p.id === producto.id);
    if (existe) {
      existe.cantidad++;
    } else {
      carrito.push({ ...producto, cantidad: 1 });
    }
    localStorage.setItem('carrito', JSON.stringify(carrito));
  }

  getColorHex(color: string): string {
  const colores: { [key: string]: string } = {
    'Negro': '#1a1a1a',
    'Blanco': '#f5f5f5',
    'Gris': '#808080',
    'Café': '#8B4513',
    'Azul': '#1e3a8a',
    'Azul Navy': '#1e3a8a',
    'Verde Oliva': '#556B2F',
    'Rojo': '#dc2626',
    'Rosa': '#ec4899',
  };
  return colores[color] || '#888';
}
}