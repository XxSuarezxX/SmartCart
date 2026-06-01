import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { CarritoService } from '../../../core/services/carrito';
import { InteraccionService } from '../../../core/services/interaccion';
import { PrecioPipe } from '../../../shared/pipes/precio-pipe';

@Component({
  selector: 'app-mi-cuenta',
  standalone: true,
  imports: [CommonModule, PrecioPipe],
  templateUrl: './mi-cuenta.html',
  styleUrls: ['./mi-cuenta.css']
})
export class MiCuentaComponent implements OnInit {
  seccionActiva = 'compras';
  username = '';
  rol = '';
  compras: any[] = [];
  favoritos: any[] = [];

  constructor(
    private authService: AuthService,
    private carritoService: CarritoService,
    private interaccionService: InteraccionService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.username = this.authService.getUsername() || '';
    this.rol = this.authService.getRol() || 'CLIENTE';
    this.cargarFavoritos();
    this.cargarCompras();
  }

  cargarFavoritos() {
    const userId = this.authService.getUserId();
    this.interaccionService.getLikes(userId).subscribe({
      next: (data: any[]) => {
        this.favoritos = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.favoritos = [];
        this.cdr.detectChanges();
      }
    });
  }

  cargarCompras() {
  const userId = this.authService.getUserId();
  this.carritoService.getHistorial(userId).subscribe({
    next: (data: any[]) => {
      this.compras = data;
      console.log('Compras:', JSON.stringify(data)); // ← agrega esto
      this.cdr.detectChanges();
    },
    error: () => {
      this.compras = [];
      this.cdr.detectChanges();
    }
  });
}

  setSeccion(seccion: string) {
    this.seccionActiva = seccion;
    this.cdr.detectChanges();
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  irAProducto(id: number) {
    this.router.navigate(['/producto', id]);
  }

  toggleDetalle(compra: any) {
  compra.abierto = !compra.abierto;
  this.cdr.detectChanges();
}
}