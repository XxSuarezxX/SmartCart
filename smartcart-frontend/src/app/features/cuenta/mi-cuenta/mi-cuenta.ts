import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-mi-cuenta',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mi-cuenta.html',
  styleUrls: ['./mi-cuenta.css']
})
export class MiCuentaComponent implements OnInit {
  seccionActiva = 'compras';
  username = '';
  email = '';
  rol = '';
  favoritos: any[] = [];

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.username = this.authService.getUsername() || '';
    const carrito = JSON.parse(localStorage.getItem('carrito') || '[]');
    this.favoritos = JSON.parse(localStorage.getItem('favoritos') || '[]');
    this.username = this.authService.getUsername() || '';
    this.rol = this.authService.getRol() || 'CLIENTE';
    this.cdr.detectChanges();
  }

  setSeccion(seccion: string) {
    this.seccionActiva = seccion;
    this.cdr.detectChanges();
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
  
}