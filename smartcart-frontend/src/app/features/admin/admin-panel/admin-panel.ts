import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductoService } from '../../../core/services/producto';
import { AuthService } from '../../../core/services/auth';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { PrecioPipe } from '../../../shared/pipes/precio-pipe';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, PrecioPipe],
  templateUrl: './admin-panel.html',
  styleUrls: ['./admin-panel.css']
})
export class AdminPanelComponent implements OnInit {
  seccionActiva = 'dashboard';
  adminName = '';
  ranking: any[] = [];
  productos: any[] = [];
  categorias: any[] = [];
  productoEditando: any = null;
  mostrarModalEditar = false;
  nuevaCategoria = { nombre: '' };
  exitoCategoria = '';
  errorCategoria = '';

  producto: any = {
    nombre: '',
    descripcion: '',
    precio: 0,
    stock: 0,
    urlImagen: '',
    colores: '',
    tallas: '',
    categoria: null
  };


  exito = '';
  error = '';

  private apiUrl = 'http://localhost:8080';

  constructor(
    private productoService: ProductoService,
    private authService: AuthService,
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.adminName = this.authService.getUsername() || 'Admin';
    this.cargarRanking();
    this.cargarProductos();
    this.productoService.getCategorias().subscribe((data: any[]) => {
      this.categorias = data;
      this.cdr.detectChanges();
    });
  }

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.authService.getToken()}`
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
    };
    return colores[color] || '#888';
  }

  cargarRanking() {
    this.http.get<any[]>(`${this.apiUrl}/api/interacciones/admin/ranking`, {
      headers: this.getHeaders()
    }).subscribe({
      next: (data: any[]) => {
        this.ranking = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.ranking = [];
        this.cdr.detectChanges();
      }
    });
  }

  cargarProductos() {
    this.productoService.getProductos().subscribe((data: any[]) => {
      this.productos = data;
      this.cdr.detectChanges();
    });
  }

  setSeccion(seccion: string) {
    this.seccionActiva = seccion;
    this.cdr.detectChanges();
  }

  crearProducto() {
    this.productoService.crearProducto(this.producto).subscribe({
      next: () => {
        this.exito = 'Producto creado exitosamente';
        this.error = '';
        this.producto = { nombre: '', descripcion: '', precio: 0, stock: 0, urlImagen: '', colores: '', tallas: '', categoria: null };
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Error al crear el producto';
        this.exito = '';
        this.cdr.detectChanges();
      }
    });
  }

  editarProducto(producto: any) {
    this.productoEditando = { ...producto, categoria: { ...producto.categoria } };
    this.mostrarModalEditar = true;
    this.cdr.detectChanges();
  }

  guardarEdicion() {
    this.productoService.editarProducto(this.productoEditando.id, this.productoEditando).subscribe({
      next: () => {
        this.mostrarModalEditar = false;
        this.productoEditando = null;
        this.cargarProductos();
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Error al editar el producto';
        this.cdr.detectChanges();
      }
    });
  }

  cancelarEdicion() {
    this.mostrarModalEditar = false;
    this.productoEditando = null;
    this.cdr.detectChanges();
  }

  eliminarProducto(id: number) {
    if (!confirm('¿Seguro que quieres eliminar este producto?')) return;
    this.productoService.eliminarProducto(id).subscribe({
      next: () => {
        this.cargarProductos();
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Error al eliminar el producto';
        this.cdr.detectChanges();
      }
    });
  }

  crearCategoria() {
    this.productoService.crearCategoria(this.nuevaCategoria).subscribe({
      next: () => {
        this.exitoCategoria = 'Categoría creada';
        this.errorCategoria = '';
        this.nuevaCategoria = { nombre: '' };
        this.productoService.getCategorias().subscribe((data: any[]) => {
          this.categorias = data;
          this.cdr.detectChanges();
        });
      },
      error: () => {
        this.errorCategoria = 'Error al crear categoría';
        this.exitoCategoria = '';
        this.cdr.detectChanges();
      }
    });
  }

  eliminarCategoria(id: number) {
    if (!confirm('¿Seguro que quieres eliminar esta categoría?')) return;
    this.productoService.eliminarCategoria(id).subscribe({
      next: () => {
        this.productoService.getCategorias().subscribe((data: any[]) => {
          this.categorias = data;
          this.cdr.detectChanges();
        });
      },
      error: () => {
        this.errorCategoria = 'Error al eliminar categoría';
        this.cdr.detectChanges();
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}