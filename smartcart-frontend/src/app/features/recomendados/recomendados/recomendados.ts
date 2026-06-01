import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { InteraccionService } from '../../../core/services/interaccion';
import { ProductoService } from '../../../core/services/producto';
import { AuthService } from '../../../core/services/auth';
import { PrecioPipe } from '../../../shared/pipes/precio-pipe';

@Component({
  selector: 'app-recomendados',
  standalone: true,
  imports: [CommonModule, PrecioPipe],
  templateUrl: './recomendados.html',
  styleUrls: ['./recomendados.css']
})
export class RecomendadosComponent implements OnInit {
  recomendados: any[] = [];
  productosPopulares: any[] = [];
  cargando = true;
  isLoggedIn = false;

  constructor(
    private interaccionService: InteraccionService,
    private productoService: ProductoService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn();

    if (this.isLoggedIn) {
      const userId = this.authService.getUserId();
      this.interaccionService.getSugeridos(userId).subscribe({
        next: (data: any[]) => {
          this.recomendados = data.map(p => ({ ...p, liked: false }));
          this.marcarFavoritos();
          this.cargando = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.cargando = false;
          this.cargarPopulares();
        }
      });
    } else {
      this.cargarPopulares();
    }
  }

  cargarPopulares() {
    this.productoService.getProductos().subscribe((data: any[]) => {
      this.productosPopulares = data.slice(0, 8).map(p => ({ ...p, liked: false }));
      this.cargando = false;
      this.cdr.detectChanges();
    });
  }

  // Marca como likeados los productos recomendados que ya están en favoritos
  private marcarFavoritos() {
    if (!this.isLoggedIn) return;
    const userId = this.authService.getUserId();
    this.interaccionService.getLikes(userId).subscribe(likes => {
      const ids = new Set(likes.map((p: any) => p.id));
      this.recomendados.forEach(p => p.liked = ids.has(p.id));
      this.cdr.detectChanges();
    });
  }

  irADetalle(producto: any) {
    if (this.isLoggedIn && producto.categoria?.id) {
      const userId = this.authService.getUserId();
      // Si en el futuro manejas clicks, aquí registrarías la vista.
    }
    this.router.navigate(['/producto', producto.id]);
  }

  toggleLike(event: Event, producto: any) {
    event.stopPropagation();
    if (!this.isLoggedIn) {
      this.router.navigate(['/login']);
      return;
    }

    producto.liked = !producto.liked;

    const userId = this.authService.getUserId();
    if (producto.liked) {
      if (producto.categoria?.id) {
        // Enviamos el 'LIKE' exacto que tu Java mapea
        this.interaccionService.registrarInteraccion(
          userId,
          producto.categoria.id,
          producto.id,
          'LIKE'
        ).subscribe();
      }
    } else {
      this.interaccionService.quitarLike(userId, producto.id).subscribe();
    }
    this.cdr.detectChanges();
  }

  getPrimeraImagen(urlImagen: string): string {
    if (!urlImagen) return '';
    return urlImagen.split(',')[0].trim();
  }

  irACatalogo() {
    this.router.navigate(['/catalogo']);
  }

  irALogin() {
    this.router.navigate(['/login']);
  }
}
