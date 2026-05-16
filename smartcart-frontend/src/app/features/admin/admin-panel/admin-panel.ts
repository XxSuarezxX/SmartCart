import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductoService } from '../../../core/services/producto';
import { AuthService } from '../../../core/services/auth';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-panel.html',
  styleUrls: ['./admin-panel.css']
})
export class AdminPanelComponent {
  producto = {
    nombre: '',
    descripcion: '',
    precio: 0,
    stock: 0,
    url_imagen: '',
    categoria: { nombre: '' }
  };

  exito = '';
  error = '';

  constructor(
    private productoService: ProductoService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  crearProducto() {
    this.productoService.crearProducto(this.producto).subscribe({
      next: () => {
        this.exito = 'Producto creado exitosamente';
        this.error = '';
        this.producto = { nombre: '', descripcion: '', precio: 0, stock: 0, url_imagen: '', categoria: { nombre: '' } };
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Error al crear el producto';
        this.exito = '';
        this.cdr.detectChanges();
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}