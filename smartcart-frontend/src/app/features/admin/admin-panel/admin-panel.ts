import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth';
import { AdminService } from '../../../core/services/admin';
import { ProductoService } from '../../../core/services/producto';
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

  dashboard: any = null;
  productos: any[] = [];
  categorias: any[] = [];
  ranking: any[] = [];
  usuarios: any[] = [];
  sinStockCount = 0;

  producto: any = { nombre: '', descripcion: '', precio: null, stock: null, urlImagen: '', categoria: null, colores: '', tallas: '' };
  nuevaCategoria: any = { nombre: '' };
  productoEditando: any = {};
  mostrarModalEditar = false;
  exito = '';
  error = '';
  exitoCategoria = '';
  errorCategoria = '';
  resultadoEliminar: { mensaje?: string; productos?: string[] } = {};

  // CSV
  archivoCSV: File | null = null;
  exitoCSV = '';
  errorCSV = '';
  cargandoCSV = false;
  private dashboardInterval: any = null;

  constructor(
    private authService: AuthService,
    private adminService: AdminService,
    private productoService: ProductoService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private http: HttpClient
  ) { }

  ngOnInit() {
    this.adminName = this.authService.getUsername() || 'Admin';
    this.cargarDashboard();
    this.cargarProductos();
    this.cargarCategorias();
    this.authService.getUsuarios().subscribe((data: any[]) => {
      this.usuarios = data.filter(u => u.rol === 'CLIENTE');
      this.cdr.detectChanges();
    });
  }

  cargarDashboard() {
    this.adminService.getDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.ranking = data?.topProductos || [];
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error dashboard:', err)
    });
  }

  cargarProductos() {
    this.productoService.getProductos().subscribe((data: any[]) => {
      this.productos = data;
      this.sinStockCount = data.filter(p => p.stock === 0).length;
      this.cdr.detectChanges();
    });
  }

  cargarCategorias() {
    this.productoService.getCategorias().subscribe({
      next: (data) => {
        this.categorias = data;
        this.cdr.detectChanges();
      },
      error: () => { }
    });
  }

  setSeccion(s: string) {
    this.seccionActiva = s;
    if (s === 'dashboard') {
      this.cargarDashboard();
      this.cargarProductos();
      if (!this.dashboardInterval) {
        this.dashboardInterval = setInterval(() => {
          this.cargarDashboard();
          this.cargarProductos();
        }, 7000);
      }
    } else {
      if (this.dashboardInterval) {
        clearInterval(this.dashboardInterval);
        this.dashboardInterval = null;
      }
    }
    this.cdr.detectChanges();
  }

  ngOnDestroy() {
    if (this.dashboardInterval) {
      clearInterval(this.dashboardInterval);
      this.dashboardInterval = null;
    }
  }

  crearProducto() {
    this.productoService.crearProducto(this.producto).subscribe({
      next: () => {
        this.exito = 'Producto creado correctamente';
        this.error = '';
        this.producto = { nombre: '', descripcion: '', precio: null, stock: null, urlImagen: '', categoria: null, colores: '', tallas: '' };
        this.cargarProductos();
        this.cdr.detectChanges();
      },
      error: () => { this.error = 'Error al crear el producto'; this.exito = ''; this.cdr.detectChanges(); }
    });
  }

  crearCategoria() {
    // Verificar token y rol antes de enviar
    if (!this.authService.isLoggedIn()) {
      this.errorCategoria = 'No autenticado: inicia sesión como administrador';
      this.exitoCategoria = '';
      this.cdr.detectChanges();
      return;
    }

    if (!this.authService.isAdmin()) {
      this.errorCategoria = 'No autorizado: necesitas rol ADMIN';
      this.exitoCategoria = '';
      this.cdr.detectChanges();
      return;
    }

    this.productoService.crearCategoria(this.nuevaCategoria).subscribe({
      next: () => {
        this.exitoCategoria = 'Categoría creada';
        this.errorCategoria = '';
        this.nuevaCategoria = { nombre: '' };
        this.cargarCategorias();
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        if (err?.status === 403) {
          this.errorCategoria = 'Servidor: no autorizado para crear categorías';
        } else {
          this.errorCategoria = 'Error al crear categoría';
        }
        this.exitoCategoria = '';
        this.cdr.detectChanges();
      }
    });
  }

  eliminarProducto(id: number) {
    if (!confirm('¿Eliminar este producto?')) return;
    this.productoService.eliminarProducto(id).subscribe({
      next: () => { this.cargarProductos(); this.cdr.detectChanges(); },
      error: () => { }
    });
  }

  eliminarCategoria(id: number, nombre: string) {
    const productosDeCategoria = this.productos.filter(p => p.categoria?.id === id);
    const cantidad = productosDeCategoria.length;
    let confirmMsg = `¿Estás seguro de eliminar la categoría "${nombre}"?`;

    if (cantidad > 0) {
      const nombres = productosDeCategoria.slice(0, 5).map(p => `- ${p.nombre}`).join('\n');
      const sufijo = cantidad > 5 ? `\n- y ${cantidad - 5} más` : '';
      confirmMsg += `\n\nSe eliminarán ${cantidad} productos de esta categoría:\n${nombres}${sufijo}`;
    } else {
      confirmMsg += `\n\nNo hay productos asociados con esta categoría.`;
    }

    if (!confirm(confirmMsg)) return;

    this.productoService.eliminarCategoria(id).subscribe({
      next: (response: any) => {
        const text = typeof response === 'string' ? response : JSON.stringify(response);
        this.exitoCategoria = text;
        this.errorCategoria = '';
        // parse response to extract product list if present
        const idx = text.indexOf('Productos:');
        if (idx >= 0) {
          const mensaje = text.substring(0, idx).trim();
          const listaRaw = text.substring(idx + 'Productos:'.length).trim();
          const productos = listaRaw.split(',').map((s: string) => s.trim()).filter((s: string) => s.length > 0);
          this.resultadoEliminar = { mensaje, productos };
        } else {
          this.resultadoEliminar = { mensaje: text };
        }
        this.cargarCategorias();
        this.cargarProductos();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al eliminar categoría:', err);
        const backendMessage = typeof err.error === 'string' && err.error.trim() ? err.error : null;
        const text = backendMessage || ('Error al eliminar categoría');
        this.errorCategoria = text;
        // try to parse products from backend error message as well
        const idx = text.indexOf('Productos:');
        if (idx >= 0) {
          const mensaje = text.substring(0, idx).trim();
          const listaRaw = text.substring(idx + 'Productos:'.length).trim();
          const productos = listaRaw.split(',').map((s: string) => s.trim()).filter((s: string) => s.length > 0);
          this.resultadoEliminar = { mensaje, productos };
        } else {
          this.resultadoEliminar = { mensaje: text };
        }
        this.exitoCategoria = '';
        this.cdr.detectChanges();
      }
    });
  }

  editarProducto(p: any) {
    this.productoEditando = { ...p };
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
    this.cdr.detectChanges();
  }

  // CSV
  onFileSelected(event: any) {
    this.archivoCSV = event.target.files[0] || null;
    this.exitoCSV = '';
    this.errorCSV = '';
    this.cdr.detectChanges();
  }

  onFileDrop(event: any) {
    event.preventDefault();
    this.archivoCSV = event.dataTransfer.files[0] || null;
    this.exitoCSV = '';
    this.errorCSV = '';
    this.cdr.detectChanges();
  }

  subirCSV() {
    if (!this.archivoCSV) return;
    this.cargandoCSV = true;
    this.exitoCSV = '';
    this.errorCSV = '';

    const formData = new FormData();
    formData.append('file', this.archivoCSV);

    const headers = new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` });

    this.http.post('/productos/cargar-csv', formData, {
      headers,
      responseType: 'text'
    }).pipe(
      finalize(() => {
        this.cargandoCSV = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (response: string) => {
        console.log('CSV upload response:', response);
        this.exitoCSV = response || 'Productos cargados correctamente';
        this.archivoCSV = null;
        this.cargarProductos();
      },
      error: (err) => {
        console.error('CSV upload error:', err);
        const backendMessage = typeof err.error === 'string' && err.error.trim() ? err.error : null;
        this.errorCSV = backendMessage
          ? backendMessage
          : `Error al subir el archivo. ${err.status ? err.status + ' ' + err.statusText : err.message}`;
      }
    });
  }

  getColorHex(nombre: string): string {
    const map: any = {
      negro: '#1a1a1a', blanco: '#f5f5f5', gris: '#9e9e9e',
      rojo: '#e53935', azul: '#1e88e5', verde: '#43a047',
      amarillo: '#fdd835', naranja: '#fb8c00', rosa: '#e91e8c',
      morado: '#8e24aa', beige: '#d7ccc8', cafe: '#6d4c41'
    };
    return map[nombre.toLowerCase()] || '#888';
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }


}