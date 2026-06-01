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
      this.productos = data.map(p => ({
        ...p,
        liked: false,
        colorActivo: p.colores ? p.colores.split(',')[0].trim() : '',
        imagenActiva: 0
      }));
      this.filtrar();
      this.marcarFavoritos();
      this.cdr.detectChanges();
    });
  }

  // Marca como likeados los productos que el usuario ya tiene en favoritos
  private marcarFavoritos() {
    if (!this.authService.isLoggedIn()) return;
    const userId = this.authService.getUserId();
    this.interaccionService.getLikes(userId).subscribe(likes => {
      const ids = new Set(likes.map((p: any) => p.id));
      this.productos.forEach(p => p.liked = ids.has(p.id));
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

    const usuarioId = this.authService.getUserId();
    if (producto.liked) {
      if (producto.categoria?.id) {
        this.interaccionService.registrarInteraccion(
          usuarioId, producto.categoria.id, producto.id, 'LIKE'
        ).subscribe();
      }
    } else {
      this.interaccionService.quitarLike(usuarioId, producto.id).subscribe();
    }
    this.cdr.detectChanges();
  }

  irADetalle(id: number) {
    this.router.navigate(['/producto', id]);
  }

  agregarCarrito(producto: any) {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    const userId = this.authService.getUserId();
    // Lo agregamos via backend
    import('../../core/services/carrito').then(m => {
      // Por ahora navegamos al detalle para agregar
      this.router.navigate(['/producto', producto.id]);
    });
  }

  getPrimeraImagen(urlImagen: string): string {
    if (!urlImagen) return '';
    return urlImagen.split(',')[0].trim();
  }

  getPrimeraImagenPorIndice(urlImagen: string, index: number): string {
  if (!urlImagen) return '';
  const imagenes = urlImagen.split(',').map(u => u.trim());
  return imagenes[index] || imagenes[0] || '';
}

  cambiarColorTarjeta(evento: Event, producto: any, color: string, index: number) {
    evento.stopPropagation();
    producto.colorActivo = color;
    producto.imagenActiva = index;
    this.cdr.detectChanges();
  }

  getColorHex(color: string): string {
  const colores: { [key: string]: string } = {
    'negro': '#1a1a1a',
    'blanco': '#f5f5f5',
    'gris': '#9e9e9e',
    'gris oscuro': '#4a4a4a',
    'gris claro': '#d4d4d4',
    'cafe': '#8B4513',
    'café': '#8B4513',
    'cafe claro': '#c8a07a',
    'cafe oscuro': '#5c2e00',
    'marron': '#795548',
    'marrón': '#795548',
    'azul': '#1e88e5',
    'azul navy': '#1e3a8a',
    'azul claro': '#64b5f6',
    'azul oscuro': '#0d47a1',
    'azul cielo': '#87ceeb',
    'azul petroleo': '#006064',
    'verde': '#43a047',
    'verde oliva': '#556B2F',
    'verde militar': '#4a5240',
    'verde menta': '#98ff98',
    'verde oscuro': '#1b5e20',
    'verde claro': '#a5d6a7',
    'rojo': '#dc2626',
    'rojo oscuro': '#b71c1c',
    'rojo vino': '#7b1c2c',
    'vino': '#7b1c2c',
    'bordo': '#800020',
    'burdeos': '#800020',
    'rosa': '#ec4899',
    'rosa claro': '#f8bbd0',
    'rosa oscuro': '#c2185b',
    'fucsia': '#ff006e',
    'naranja': '#fb8c00',
    'naranja claro': '#ffcc80',
    'naranja oscuro': '#e65100',
    'amarillo': '#fdd835',
    'amarillo claro': '#fff9c4',
    'mostaza': '#f0a500',
    'dorado': '#ffd700',
    'morado': '#8e24aa',
    'lila': '#ce93d8',
    'violeta': '#7b1fa2',
    'lavanda': '#b39ddb',
    'beige': '#d7ccc8',
    'crema': '#fffdd0',
    'arena': '#f5deb3',
    'salmon': '#fa8072',
    'coral': '#ff6b6b',
    'turquesa': '#00bcd4',
    'menta': '#98ff98',
    'plateado': '#c0c0c0',
    'plata': '#c0c0c0',
    'cobre': '#b87333',
  };
  return colores[color.trim().toLowerCase()] || '#888';
}
}