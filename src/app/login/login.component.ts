// src/app/login/login.component.ts
import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, Validators, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { ApiService } from '../services/api';

type ToastType = 'success' | 'error' | 'info';
type Toast = { id: string; type: ToastType; title: string; message: string };

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private api = inject(ApiService);

  year = new Date().getFullYear();

  loading = signal(false);
  submitted = false;

  // Error general (para mostrar arriba del form)
  authError = signal<string | null>(null);

  // Toasts
  toasts = signal<Toast[]>([]);

  form: FormGroup = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  private setFormEnabled(enabled: boolean) {
    if (enabled) this.form.enable({ emitEvent: false });
    else this.form.disable({ emitEvent: false });
  }

  private pushToast(type: ToastType, title: string, message: string) {
    const id = (crypto as any)?.randomUUID?.() ?? String(Date.now() + Math.random());
    const toast: Toast = { id, type, title, message };
    this.toasts.update(list => [toast, ...list].slice(0, 3)); // max 3 visibles
    setTimeout(() => this.dismissToast(id), 5000);
  }

  dismissToast(id: string) {
    this.toasts.update(list => list.filter(t => t.id !== id));
  }

  onForgot(ev: Event) {
    ev.preventDefault();
    this.pushToast(
      'info',
      'Recuperación de contraseña',
      'Contacta al administrador del sistema para restablecer tu contraseña.'
    );
  }

  onSubmit(): void {
    this.submitted = true;
    this.authError.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.pushToast('error', 'Validación', 'Revisa los campos obligatorios para continuar.');
      return;
    }

    const username = (this.form.value.username ?? '').trim();
    const password = this.form.value.password ?? '';

    this.loading.set(true);
    this.setFormEnabled(false);

    this.api.login({ username, password }).subscribe({
      next: (res: any) => {
        this.loading.set(false);
        this.setFormEnabled(true);

        if (res?.ok && res?.user) {
          localStorage.setItem('logged', '1');
          localStorage.setItem('userId', String(res.user.id));
          localStorage.setItem('role', String(res.user.rol || '').toUpperCase());

          this.pushToast('success', 'Bienvenido', 'Ingreso exitoso.');

          if (res.user.mustChangePassword) {
            this.router.navigateByUrl('/cambiar-clave');
          } else {
            this.router.navigateByUrl('/mapa');
          }
          return;
        }

        const msg = res?.message || 'Usuario o contraseña incorrectos. Verifica e intenta de nuevo.';
        this.authError.set(msg);
        this.pushToast('error', 'Inicio de sesión', msg);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.setFormEnabled(true);

        const msg =
          err?.error?.message ||
          (err.status === 0
            ? 'No hay conexión con el servidor. Verifica tu red o intenta más tarde.'
            : `No fue posible iniciar sesión. Código: ${err.status}`);

        this.authError.set(msg);
        this.pushToast('error', 'Error', msg);
      }
    });
  }
}
