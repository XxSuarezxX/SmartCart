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

  irADetalle(id: number) {
    this.router.navigate(['/producto', id]);
  }

  toggleLike(event: Event, producto: any) {
    event.stopPropagation();
    if (!this.isLoggedIn) {
      this.router.navigate(['/login']);
      return;
    }
    producto.liked = !producto.liked;
    if (producto.liked) {
      const userId = this.authService.getUserId();
      this.interaccionService.registrarInteraccion(
        userId, producto.id, producto.categoria?.id
      ).subscribe();
    }
    this.cdr.detectChanges();
  }

  irACatalogo() {
    this.router.navigate(['/catalogo']);
  }

  irALogin() {
    this.router.navigate(['/login']);
  }
}