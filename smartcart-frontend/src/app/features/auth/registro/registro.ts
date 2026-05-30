import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrls: ['./registro.css']
})
export class RegistroComponent {
  nombre = '';
  email = '';
  password = '';
  error = '';
  mostrarPassword = false;
  
  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  onRegister() {
    this.authService.registro(this.nombre, this.email, this.password).subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: () => {
        this.error = 'Error al registrarse, intenta de nuevo';
        this.cdr.detectChanges();
      }
    });
  }
}