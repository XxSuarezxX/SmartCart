import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductoService } from '../../../core/services/producto';
import { InteraccionService } from '../../../core/services/interaccion';
import { AuthService } from '../../../core/services/auth';
import { PrecioPipe } from '../../../shared/pipes/precio-pipe';
import { CarritoService } from '../../../core/services/carrito';

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
    const userId = this.authService.getUserId();
    if (this.liked) {
      if (this.producto.categoria?.id) {
        this.interaccionService.registrarInteraccion(
          userId, this.producto.categoria.id, this.producto.id, 'LIKE'
        ).subscribe();
      }
    } else {
      this.interaccionService.quitarLike(userId, this.producto.id).subscribe();
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
    const userId = this.authService.getUserId();
    this.carritoService.agregarProducto(userId, this.producto.id, 1).subscribe({
      next: () => {
        if (this.producto.categoria?.id) {
          this.interaccionService.registrarInteraccion(
            userId, this.producto.categoria.id, this.producto.id, 'CARRITO'
          ).subscribe();
        }
        this.router.navigate(['/carrito']);
      },
      error: () => {
        alert('Error al agregar al carrito');
      }
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
      'Negro': '#1a1a1a',
      'Blanco': '#f5f5f5',
      'Gris': '#808080',
      'Café': '#8B4513',
      'Azul': '#1e3a8a',
      'Azul Navy': '#1e3a8a',
      'Verde Oliva': '#556B2F',
      'Rojo': '#dc2626',
      'Rosa': '#ec4899',
      'Amarillo': '#eab308',
      'Naranja': '#f97316',
      'Morado': '#7c3aed',
    };
    return colores[color] || '#888';
  }

  volver() {
    window.history.back();
  }
}