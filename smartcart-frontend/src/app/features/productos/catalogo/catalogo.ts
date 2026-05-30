import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductoService } from '../../../core/services/producto';
import { InteraccionService } from '../../../core/services/interaccion';
import { AuthService } from '../../../core/services/auth';
import { Router } from '@angular/router';
import { PrecioPipe } from '../../../shared/pipes/precio-pipe';

@Component({
  selector: 'app-catalogo',
  standalone: true,
  imports: [CommonModule, FormsModule, PrecioPipe],
  templateUrl: './catalogo.html',
  styleUrls: ['./catalogo.css']
})
export class CatalogoComponent implements OnInit {
  productos: any[] = [];
  productosFiltrados: any[] = [];
  categorias: any[] = [];

  categoriaSeleccionada = '';
  ordenSeleccionado = 'relevante';
  precioSeleccionado = '';

  constructor(
    private productoService: ProductoService,
    private interaccionService: InteraccionService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.productoService.getProductos().subscribe(data => {
      this.productos = data.map(p => ({ ...p, liked: false }));
      this.productosFiltrados = [...this.productos];
      this.cdr.detectChanges();
    });

    this.productoService.getCategorias().subscribe(data => {
      this.categorias = data;
      this.cdr.detectChanges();
    });
  }

  aplicarFiltros() {
    let resultado = [...this.productos];

    if (this.categoriaSeleccionada) {
      resultado = resultado.filter(p => p.categoria?.nombre === this.categoriaSeleccionada);
    }

    if (this.precioSeleccionado === 'menos50') {
      resultado = resultado.filter(p => p.precio < 50000);
    } else if (this.precioSeleccionado === '50a100') {
      resultado = resultado.filter(p => p.precio >= 50000 && p.precio <= 100000);
    } else if (this.precioSeleccionado === 'mas100') {
      resultado = resultado.filter(p => p.precio > 100000);
    }

    if (this.ordenSeleccionado === 'precio-asc') {
      resultado.sort((a, b) => a.precio - b.precio);
    } else if (this.ordenSeleccionado === 'precio-desc') {
      resultado.sort((a, b) => b.precio - a.precio);
    } else if (this.ordenSeleccionado === 'nombre') {
      resultado.sort((a, b) => a.nombre.localeCompare(b.nombre));
    }

    this.productosFiltrados = resultado;
    this.cdr.detectChanges();
  }

  limpiarFiltros() {
    this.categoriaSeleccionada = '';
    this.precioSeleccionado = '';
    this.ordenSeleccionado = 'relevante';
    this.productosFiltrados = [...this.productos];
    this.cdr.detectChanges();
  }

  irADetalle(id: number) {
    this.router.navigate(['/producto', id]);
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

  getPrimeraImagen(urlImagen: string): string {
    if (!urlImagen) return '';
    return urlImagen.split(',')[0].trim();
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
}