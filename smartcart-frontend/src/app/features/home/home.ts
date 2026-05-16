import { Component, OnInit, ChangeDetectorRef, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductoService } from '../../core/services/producto';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent implements OnInit {
  productos: any[] = [];
  productosFiltrados: any[] = [];
  categorias: any[] = [];

  private _categoria = '';

  @Input() set categoria(valor: string) {
    this._categoria = valor;
    this.filtrar();
  }

  constructor(
    private productoService: ProductoService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.productoService.getProductos().subscribe(data => {
      this.productos = data;
      this.filtrar();
      this.cdr.detectChanges();
    });
  }

  filtrar() {
    if (!this._categoria) {
      this.productosFiltrados = this.productos;
    } else {
      this.productosFiltrados = this.productos.filter(
        p => p.categoria?.nombre === this._categoria
      );
    }
    this.cdr.detectChanges();
  }
}