import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models/user.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  credentials: LoginRequest = {
    username: '',
    password: '',
  };

  errorMessage: string = '';
  showInactiveModal: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  onLogin(): void {
    this.errorMessage = '';
    this.showInactiveModal = false;

    // Validación básica
    if (!this.credentials.username || !this.credentials.password) {
      this.errorMessage = 'Por favor ingresa usuario y contraseña';
      return;
    }

    // Llamar al servicio de autenticación
    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        console.log('Login exitoso:', response);
        // Redirigir al dashboard después del login
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        console.error('Error en login:', error);
        console.log('error.error:', error.error);
        console.log('error.error?.message:', error.error?.message);
        console.log('error.status:', error.status);

        let errorMsg = '';
        if (error.error && typeof error.error === 'object' && error.error.message) {
          errorMsg = error.error.message;
        } else if (typeof error.error === 'string') {
          errorMsg = error.error;
        } else if (error.message) {
          errorMsg = error.message;
        }

        console.log('Extracted errorMsg:', errorMsg);

        if (
          errorMsg.toLowerCase().includes('inactiv') ||
          errorMsg.toLowerCase().includes('desactiv')
        ) {
          console.log('Showing inactive modal');
          this.showInactiveModal = true;
          this.cdr.detectChanges();
        } else {
          console.log('Showing generic error');
          this.errorMessage = 'Usuario o contraseña incorrectos';
          this.cdr.detectChanges();
        }
      },
    });
  }

  closeInactiveModal(): void {
    this.showInactiveModal = false;
  }
}
