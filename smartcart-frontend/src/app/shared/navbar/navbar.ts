import { Component, OnInit, ChangeDetectionStrategy, Output, EventEmitter, HostListener, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductoService } from '../../core/services/producto';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NavbarComponent implements OnInit {
  categorias: any[] = [];
  menuAbierto = false;
  busqueda = '';
  likesCount = 0;
  carritoCount = 0;

  @Output() categoriaSeleccionada = new EventEmitter<string>();

  get isLoggedIn(): boolean { return this.authService.isLoggedIn(); }
  get isAdmin(): boolean { return this.authService.isAdmin(); }
  get username(): string { return this.authService.getUsername() || ''; }

  constructor(
    private productoService: ProductoService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.productoService.getCategorias().subscribe(data => {
      this.categorias = data;
      this.cdr.markForCheck();
    });
  }

  toggleMenu() {
    this.menuAbierto = !this.menuAbierto;
    this.cdr.markForCheck();
  }

  logout() {
    this.authService.logout();
    this.menuAbierto = false;
    this.router.navigate(['/']);
    this.cdr.markForCheck();
  }

  @HostListener('document:click', ['$event'])
  cerrarMenu(event: Event) {
    const target = event.target as HTMLElement;
    if (!target.closest('.account')) {
      this.menuAbierto = false;
      this.cdr.markForCheck();
    }
  }
}