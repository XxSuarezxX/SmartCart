import { Component, OnInit, ChangeDetectionStrategy, Output, EventEmitter, HostListener, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductoService } from '../../core/services/producto';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NavbarComponent implements OnInit {
  categorias: any[] = [];
  menuAbierto = false;
  @Output() categoriaSeleccionada = new EventEmitter<string>();

  constructor(
    private productoService: ProductoService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.productoService.getCategorias().subscribe(data => {
      this.categorias = data;
      this.cdr.markForCheck();
    });
  }

  filtrar(nombre: string) {
    this.categoriaSeleccionada.emit(nombre);
  }

  mostrarTodos() {
    this.categoriaSeleccionada.emit('');
  }

  toggleMenu() {
    this.menuAbierto = !this.menuAbierto;
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