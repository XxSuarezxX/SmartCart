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
        this.cargando = false;
        console.log('Producto recibido:', data);
        this.producto = data;
        console.log('URL imagen:', data.urlImagen);
        this.producto = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargando = false;
        this.router.navigate(['/']);
      }

    });
  }

  seleccionarColor(color: string) {
    this.colorSeleccionado = color;
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
        userId, this.producto.id, this.producto.categoria?.id
      ).subscribe();

      const favoritos = JSON.parse(localStorage.getItem('favoritos') || '[]');
      const existe = favoritos.find((p: any) => p.id === this.producto.id);
      if (!existe) {
        favoritos.push(this.producto);
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
    const userId = this.authService.getUserId();
    this.carritoService.agregarProducto(userId, this.producto.id, 1).subscribe({
      next: () => {
        this.router.navigate(['/carrito']);
      },
      error: () => {
        alert('Error al agregar al carrito');
      }
    });
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