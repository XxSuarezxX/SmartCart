import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'precio', standalone: true })
export class PrecioPipe implements PipeTransform {
  transform(value: number): string {
    if (!value) return '$0';
    return '$' + value.toLocaleString('es-CO');
  }
}