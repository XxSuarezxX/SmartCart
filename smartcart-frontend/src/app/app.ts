import { Component } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { NavbarComponent } from './shared/navbar/navbar';
import { CommonModule } from '@angular/common';
import { FooterComponent } from './shared/footer/footer';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, CommonModule, FooterComponent],
  templateUrl: './app.html',
})
export class AppComponent {
  categoriaActiva = '';
  mostrarNavbar = true;

  constructor(private router: Router) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        const rutasOcultas = ['/login', '/registro'];
        this.mostrarNavbar = !rutasOcultas.includes(event.urlAfterRedirects);
      }
    });
  }

  onCategoria(nombre: string) {
    this.categoriaActiva = nombre;
  }
}