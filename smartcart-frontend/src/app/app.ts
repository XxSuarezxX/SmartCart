import { Component } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { NavbarComponent } from './shared/navbar/navbar';
import { CommonModule } from '@angular/common';
import { FooterComponent } from './shared/footer/footer';
import { AuthService } from './core/services/auth';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, CommonModule, FooterComponent],
  templateUrl: './app.html',
})
export class AppComponent {
  categoriaActiva = '';
  mostrarNavbar = true;

  constructor(private router: Router, private authService: AuthService) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        const rutasOcultas = ['/login', '/registro'];
        this.mostrarNavbar = !rutasOcultas.some(r => event.urlAfterRedirects.startsWith(r));
      }
    });
  }

  onCategoria(nombre: string) {
    this.categoriaActiva = nombre;
  }

  esAdmin(): boolean {
    return this.authService.isAdmin();
  }
}