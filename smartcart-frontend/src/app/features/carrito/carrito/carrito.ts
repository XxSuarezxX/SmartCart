import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CarritoService } from '../../../core/services/carrito';
import { AuthService } from '../../../core/services/auth';
import { InteraccionService } from '../../../core/services/interaccion';
import { PrecioPipe } from '../../../shared/pipes/precio-pipe';
import { EstadoService } from '../../../core/services/estado';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [CommonModule, FormsModule, PrecioPipe],
  templateUrl: './carrito.html',
  styleUrls: ['./carrito.css']
})
export class CarritoComponent implements OnInit {
  items: any[] = [];
  cargando = true;
  procesandoPago = false;
  pagoExitoso = false;
  error = '';

  pago = {
    numeroTarjeta: '',
    nombreTitular: '',
    fechaVencimiento: '',
    cvv: '',
    direccionEnvio: ''
  };

  constructor(
    private carritoService: CarritoService,
    private authService: AuthService,
    private interaccionService: InteraccionService,
    private router: Router,
    private estadoService: EstadoService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.cargarCarrito();
  }

  cargarCarrito() {
    const userId = this.authService.getUserId();
    this.carritoService.getCarrito(userId).subscribe({
      next: (data: any[]) => {
        this.items = data;
        this.estadoService.actualizarCarrito(data);
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargando = false;
        this.cdr.detectChanges();
      },
    });
  }

  get subtotal(): number {
    return this.items.reduce((acc, item) => acc + (item.producto.precio * item.cantidad), 0);
  }

  get total(): number {
    return this.subtotal;
  }

  actualizarCantidad(item: any, cantidad: number) {
    if (cantidad < 1) {
      this.eliminarItem(item);
      return;
    }
    this.carritoService.actualizarCantidad(item.id, cantidad).subscribe({
      next: () => {
        item.cantidad = cantidad;
        this.cdr.detectChanges();
      }
    });
  }

  eliminarItem(item: any) {
    this.carritoService.eliminarItem(item.id).subscribe({
      next: () => {
        this.items = this.items.filter(i => i.id !== item.id);
        this.estadoService.actualizarCarrito(this.items);
        this.cdr.detectChanges();
      }
    });
  }

  vaciarCarrito() {
    const userId = this.authService.getUserId();
    this.carritoService.vaciarCarrito(userId).subscribe({
      next: () => {
        this.items = [];
        this.estadoService.actualizarCarrito([]);
        this.cdr.detectChanges();
      }
    });
  }

  procesarPago() {
    if (!this.pago.numeroTarjeta || !this.pago.nombreTitular ||
      !this.pago.fechaVencimiento || !this.pago.cvv || !this.pago.direccionEnvio) {
      this.error = 'Por favor completa todos los campos incluyendo la dirección de envío';
      this.cdr.detectChanges();
      return;
    }

    this.procesandoPago = true;
    this.error = '';

    const userId = this.authService.getUserId();
    this.carritoService.procesarPago({
      usuarioId: userId,
      ...this.pago
    }).subscribe({
      next: () => {
        // Registramos una interacción de COMPRA por cada producto del carrito,
        // antes de vaciarlo.
        this.items.forEach(item => {
          const categoriaId = item.producto?.categoria?.id;
          const productoId = item.producto?.id;
          if (categoriaId && productoId) {
            this.interaccionService.registrarInteraccion(
              userId, categoriaId, productoId, 'COMPRA'
            ).subscribe();
          }
        });

        this.pagoExitoso = true;
        this.items = [];
        this.procesandoPago = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Error al procesar el pago, intenta de nuevo';
        this.procesandoPago = false;
        this.cdr.detectChanges();
      }
    });
  }

  formatearTarjeta(event: any) {
    let val = event.target.value.replace(/\D/g, '');
    val = val.match(/.{1,4}/g)?.join(' ') || val;
    this.pago.numeroTarjeta = val;
  }

  irACatalogo() {
    this.router.navigate(['/catalogo']);
  }

  formatearFecha(event: any) {
    let val = event.target.value.replace(/\D/g, ''); // solo números
    if (val.length >= 2) {
      val = val.substring(0, 2) + '/' + val.substring(2, 4);
    }
    event.target.value = val;
    this.pago.fechaVencimiento = val;
  }
}