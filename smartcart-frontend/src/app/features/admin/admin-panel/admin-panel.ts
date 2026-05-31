import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth';
import { ProductoService } from '../../../core/services/producto';
import { PrecioPipe } from '../../../shared/pipes/precio-pipe';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, PrecioPipe],
  templateUrl: './admin-panel.html',
  styleUrls: ['./admin-panel.css']
})
export class AdminPanelComponent implements OnInit, OnDestroy {
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

  archivoCSV: File | null = null;
  exitoCSV = '';
  errorCSV = '';
  cargandoCSV = false;
  private dashboardInterval: any = null;

  constructor(
    private authService: AuthService,
    private productoService: ProductoService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private http: HttpClient
  ) {}

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

  ngOnDestroy() {
    if (this.dashboardInterval) {
      clearInterval(this.dashboardInterval);
      this.dashboardInterval = null;
    }
  }

  cargarDashboard() {
    const headers = new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` });

    this.http.get<any[]>('/api/interacciones/admin/ranking', { headers }).subscribe({
      next: (data) => {
        this.ranking = data || [];
        this.cdr.detectChanges();
      },
      error: () => { this.ranking = []; }
    });

    this.http.get<any[]>('/api/pagos/todos', { headers }).subscribe({
      next: (data) => {
        if (!this.dashboard) this.dashboard = {};
        this.dashboard.pagosRecientes = (data || []).slice(0, 5).map((p: any) => ({
          clienteNombre: p.usuario?.nombre || 'Cliente',
          clienteEmail: p.usuario?.email || '',
          monto: p.montoTotal,
          estado: p.estado,
          fecha: new Date(p.fechaPago).toLocaleDateString('es-CO')
        }));
        this.dashboard.ingresosTotales = (data || []).reduce((acc: number, p: any) => acc + p.montoTotal, 0);
        this.dashboard.totalPagos = (data || []).length;
        this.cdr.detectChanges();
      },
      error: () => {
        if (!this.dashboard) this.dashboard = {};
        this.dashboard.pagosRecientes = [];
        this.dashboard.ingresosTotales = 0;
        this.dashboard.totalPagos = 0;
        this.cdr.detectChanges();
      }
    });

    this.authService.getUsuarios().subscribe({
      next: (data: any[]) => {
        const clientes = data.filter(u => u.rol === 'CLIENTE');
        if (!this.dashboard) this.dashboard = {};
        this.dashboard.totalClientes = clientes.length;
        this.dashboard.clientesRecientes = clientes.slice(0, 5).map(u => ({
          nombre: u.nombre, email: u.email, totalPagos: 0, totalGastado: 0
        }));
        this.cdr.detectChanges();
      },
      error: () => {}
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
      error: () => {}
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

  crearProducto() {
    this.productoService.crearProducto(this.producto).subscribe({
      next: () => {
        this.exito = 'Producto creado correctamente';
        this.error = '';
        this.producto = { nombre: '', descripcion: '', precio: null, stock: null, urlImagen: '', categoria: null, colores: '', tallas: '' };
        this.cargarProductos();
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Error al crear el producto';
        this.exito = '';
        this.cdr.detectChanges();
      }
    });
  }

  crearCategoria() {
    if (!this.authService.isLoggedIn()) {
      this.errorCategoria = 'No autenticado';
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
        this.errorCategoria = err?.status === 403 ? 'No autorizado' : 'Error al crear categoría';
        this.exitoCategoria = '';
        this.cdr.detectChanges();
      }
    });
  }

  eliminarProducto(id: number) {
    if (!confirm('¿Eliminar este producto?')) return;
    this.productoService.eliminarProducto(id).subscribe({
      next: () => { this.cargarProductos(); this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  eliminarCategoria(id: number, nombre: string) {
    const productosDeCategoria = this.productos.filter(p => p.categoria?.id === id);
    const cantidad = productosDeCategoria.length;
    let confirmMsg = `¿Estás seguro de eliminar la categoría "${nombre}"?`;
    if (cantidad > 0) {
      const nombres = productosDeCategoria.slice(0, 5).map(p => `- ${p.nombre}`).join('\n');
      const sufijo = cantidad > 5 ? `\n- y ${cantidad - 5} más` : '';
      confirmMsg += `\n\nSe eliminarán ${cantidad} productos:\n${nombres}${sufijo}`;
    } else {
      confirmMsg += `\n\nNo hay productos en esta categoría.`;
    }
    if (!confirm(confirmMsg)) return;

    this.productoService.eliminarCategoria(id).subscribe({
      next: (response: any) => {
        const text = typeof response === 'string' ? response : JSON.stringify(response);
        this.exitoCategoria = text;
        this.errorCategoria = '';
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
        const text = typeof err.error === 'string' && err.error.trim() ? err.error : 'Error al eliminar categoría';
        this.errorCategoria = text;
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

    this.http.post('/productos/cargar-csv', formData, { headers, responseType: 'text' }).pipe(
      finalize(() => { this.cargandoCSV = false; this.cdr.detectChanges(); })
    ).subscribe({
      next: (response: string) => {
        this.exitoCSV = response || 'Productos cargados correctamente';
        this.archivoCSV = null;
        this.cargarProductos();
      },
      error: (err) => {
        this.errorCSV = typeof err.error === 'string' && err.error.trim()
          ? err.error
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