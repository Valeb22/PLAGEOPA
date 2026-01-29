// src/app/change-password/change-password.component.ts
import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService } from '../services/api';

type ToastType = 'success' | 'error' | 'info';
type Toast = { id: string; type: ToastType; title: string; message: string };

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change-password.html',
  styleUrls: ['./change-password.css'],
})
export class ChangePasswordComponent {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private router = inject(Router);

  loading = signal(false);
  submitted = false;

  pageError = signal<string | null>(null);

  // Toasts
  toasts = signal<Toast[]>([]);

  form = this.fb.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required],
  });

  private setFormEnabled(enabled: boolean) {
    if (enabled) this.form.enable({ emitEvent: false });
    else this.form.disable({ emitEvent: false });
  }

  private pushToast(type: ToastType, title: string, message: string) {
    const id = (crypto as any)?.randomUUID?.() ?? String(Date.now() + Math.random());
    const toast: Toast = { id, type, title, message };
    this.toasts.update(list => [toast, ...list].slice(0, 3));
    setTimeout(() => this.dismissToast(id), 5000);
  }

  dismissToast(id: string) {
    this.toasts.update(list => list.filter(t => t.id !== id));
  }

  private userId(): number {
    return Number(localStorage.getItem('userId') || '0');
  }

  isInvalid(name: 'currentPassword' | 'newPassword' | 'confirmPassword'): boolean {
    const c = this.form.controls[name];
    return !!(this.submitted && c.invalid);
  }

  passwordMismatch(): boolean {
    const newP = this.form.value.newPassword || '';
    const conf = this.form.value.confirmPassword || '';
    if (!newP || !conf) return false;
    return newP !== conf;
  }

  onSubmit() {
    this.submitted = true;
    this.pageError.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.pushToast('error', 'Validación', 'Revisa los campos obligatorios para continuar.');
      return;
    }

    if (this.passwordMismatch()) {
      this.pushToast('error', 'Validación', 'La nueva contraseña y la confirmación no coinciden.');
      return;
    }

    const currentPassword = this.form.value.currentPassword || '';
    const newPassword = this.form.value.newPassword || '';

    this.loading.set(true);
    this.setFormEnabled(false);

    this.api.changePassword({
      userId: this.userId(),
      currentPassword,
      newPassword
    }).subscribe({
      next: (res: any) => {
        this.loading.set(false);
        this.setFormEnabled(true);

        if (res?.ok) {
          this.pushToast('success', 'Contraseña actualizada', 'Ya puedes ingresar a la plataforma.');
          this.router.navigateByUrl('/mapa');
          return;
        }

        const msg = res?.message || 'No se pudo actualizar la contraseña.';
        this.pageError.set(msg);
        this.pushToast('error', 'Actualización', msg);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.setFormEnabled(true);

        const msg =
          err?.error?.message ||
          (err.status === 0 ? 'No hay conexión con el servidor.' : `Error al actualizar. Código: ${err.status}`);

        this.pageError.set(msg);
        this.pushToast('error', 'Error', msg);
      }
    });
  }
}
