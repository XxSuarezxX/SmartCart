import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductoService } from '../../../core/services/producto';
import { InteraccionService } from '../../../core/services/interaccion';
import { AuthService } from '../../../core/services/auth';
import { PrecioPipe } from '../../../shared/pipes/precio-pipe';
import { CarritoService } from '../../../core/services/carrito';
import { EstadoService } from '../../../core/services/estado';

@Component({
  selector: 'app-detalle',
  standalone: true,
  imports: [CommonModule, PrecioPipe],
  templateUrl: './detalle.html',
  styleUrls: ['./detalle.css']
})
export class DetalleComponent implements OnInit {
  producto: any = null;
  colores: string[] = [];
  tallas: string[] = [];
  colorSeleccionado = '';
  tallaSeleccionada = '';
  liked = false;
  cargando = true;
  imagenes: string[] = [];
  imagenActiva = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productoService: ProductoService,
    private interaccionService: InteraccionService,
    private authService: AuthService,
    private carritoService: CarritoService,
    private estadoService: EstadoService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;

    this.productoService.getProductoPorId(+id).subscribe({
      next: (data: any) => {
        this.producto = data;
        this.colores = data.colores ? data.colores.split(',').map((c: string) => c.trim()) : [];
        this.tallas = data.tallas ? data.tallas.split(',').map((t: string) => t.trim()) : [];
        if (this.colores.length > 0) this.colorSeleccionado = this.colores[0];
        if (this.imagenes.length > 0) this.imagenActiva = 0;
        this.tallas = data.tallas ? data.tallas.split(',').map((t: string) => t.trim()) : [];
        if (this.colores.length > 0) this.colorSeleccionado = this.colores[0];
        this.cargando = false;
        console.log('Producto recibido:', data);
        this.producto = data;
        console.log('URL imagen:', data.urlImagen);
        this.producto = data;
        this.imagenes = data.urlImagen
          ? data.urlImagen.split(',').map((url: string) => url.trim()).filter((url: string) => url.length > 0)
          : [];
        if (this.imagenes.length === 0) this.imagenes = [''];
        this.marcarFavorito();
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargando = false;
        this.router.navigate(['/']);
      }

    });
  }

  // Marca el corazón si el usuario ya tiene este producto en favoritos
  private marcarFavorito() {
    if (!this.authService.isLoggedIn() || !this.producto) return;
    const userId = this.authService.getUserId();
    this.interaccionService.getLikes(userId).subscribe(likes => {
      this.liked = likes.some((p: any) => p.id === this.producto.id);
      this.cdr.detectChanges();
    });
  }

  seleccionarColor(color: string) {
    this.colorSeleccionado = color;
    const index = this.colores.indexOf(color);
    if (index !== -1 && index < this.imagenes.length) {
      this.imagenActiva = index;
    }
    this.cdr.detectChanges();
  }

  seleccionarTalla(talla: string) {
    this.tallaSeleccionada = talla;
    this.cdr.detectChanges();
  }

  toggleLike() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.liked = !this.liked;
    if (this.liked) {
      const userId = this.authService.getUserId();
      this.interaccionService.registrarInteraccion(
        userId, this.producto.id, this.producto.categoria?.id, 'LIKE'
      ).subscribe();

      const favoritos = JSON.parse(localStorage.getItem('favoritos') || '[]');
      const existe = favoritos.find((p: any) => p.id === this.producto.id);
      if (!existe) {
        favoritos.push({
          id: this.producto.id,
          nombre: this.producto.nombre,
          precio: this.producto.precio,
          urlImagen: this.producto.urlImagen  
        });
        localStorage.setItem('favoritos', JSON.stringify(favoritos));
      }
    } else {
      const favoritos = JSON.parse(localStorage.getItem('favoritos') || '[]');
      const nuevos = favoritos.filter((p: any) => p.id !== this.producto.id);
      localStorage.setItem('favoritos', JSON.stringify(nuevos));
    }
    this.cdr.detectChanges();
  }

  comprar() {
  if (!this.authService.isLoggedIn()) {
    this.router.navigate(['/login']);
    return;
  }
  if (!this.tallaSeleccionada) {
    alert('Selecciona una talla');
    return;
  }
  if (this.producto.stock <= 0) {
    alert('No hay stock disponible');
    return;
  }
  const userId = this.authService.getUserId();
  this.carritoService.agregarProducto(userId, this.producto.id, 1).subscribe({
    next: () => this.router.navigate(['/carrito']),
    error: () => alert('Error al agregar al carrito')
  });
}

  imagenAnterior() {
    this.imagenActiva = this.imagenActiva > 0 ? this.imagenActiva - 1 : this.imagenes.length - 1;
    this.cdr.detectChanges();
  }

  imagenSiguiente() {
    this.imagenActiva = this.imagenActiva < this.imagenes.length - 1 ? this.imagenActiva + 1 : 0;
    this.cdr.detectChanges();
  }

  seleccionarImagen(index: number) {
    this.imagenActiva = index;
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

  volver() {
    window.history.back();
  }
}