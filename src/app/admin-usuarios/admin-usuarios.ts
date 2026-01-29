// src/app/admin/admin-usuarios/admin-usuarios.component.ts
import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService, UserListItemDto } from '../services/api';

type ToastType = 'success' | 'error' | 'info';
type Toast = { id: string; type: ToastType; title: string; message: string; };
type PageMessage = { type: ToastType; title: string; message: string } | null;

@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-usuarios.html',
  styleUrls: ['./admin-usuarios.css'],
})
export class AdminUsuariosComponent implements OnInit {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);

  loading = signal(false);

  // Mensaje general arriba (reemplaza msgOk/msgErr “sueltos”)
  pageMessage = signal<PageMessage>(null);

  // Toasts
  toasts = signal<Toast[]>([]);

  // Confirmación eliminar
  confirmDeleteOpen = signal(false);
  pendingDelete = signal<UserListItemDto | null>(null);

  // Selección de usuario en tabla
  selectedUser = signal<UserListItemDto | null>(null);

  // Para el checkbox (no estaba en tu formReset original, lo mantenemos como señal UI)
  mustChangePassword = signal(true);

  /* =========================
     LISTADO DE USUARIOS
     ========================= */
  users  = signal<UserListItemDto[]>([]);
  filtro = signal('');

  usersFiltrados = computed(() => {
    const q = (this.filtro() || '').trim().toLowerCase();
    if (!q) return this.users();

    return this.users().filter(u =>
      (u.username || '').toLowerCase().includes(q) ||
      (u.email || '').toLowerCase().includes(q) ||
      String(u.id).includes(q)
    );
  });

  /* =========================
     FORM: CREAR USUARIO
     ========================= */
  formCreate = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email:    ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    rol:      ['USER' as 'USER' | 'ADMIN', Validators.required],
  });

  /* =========================
     FORM: RESET PASSWORD
     ========================= */
  formReset = this.fb.group({
    userId:        [null as number | null, Validators.required],
    tempPassword: ['', [Validators.required, Validators.minLength(6)]],
  });

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  /* =========================
     Helpers UI/Forms
     ========================= */
  isInvalid(group: FormGroup, controlName: string): boolean {
    const c = group.get(controlName);
    return !!(c && c.invalid && (c.touched || c.dirty));
  }

  private clearMessages() {
    this.pageMessage.set(null);
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

  private showPageMessage(type: ToastType, title: string, message: string) {
    this.pageMessage.set({ type, title, message });
  }

  /* =========================
     Selección
     ========================= */
  selectUser(u: UserListItemDto) {
    this.selectedUser.set(u);
  }

  setResetFromSelected() {
    const u = this.selectedUser();
    if (!u) return;
    this.setResetFromUser(u);
    this.pushToast('info', 'Reset', `Usuario seleccionado: ${u.username} (ID ${u.id}).`);
  }

  /* =========================
     CARGAR USUARIOS
     ========================= */
  cargarUsuarios() {
    this.clearMessages();
    this.loading.set(true);

    this.api.listUsers().subscribe({
      next: (list) => {
        this.loading.set(false);
        this.users.set(Array.isArray(list) ? list : []);

        if (!Array.isArray(list)) {
          this.showPageMessage('error', 'Listado', 'El backend no devolvió una lista válida de usuarios.');
          this.pushToast('error', 'Listado', 'Respuesta inválida del servidor.');
          return;
        }

        this.pushToast('success', 'Listado', 'Usuarios cargados correctamente.');
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);

        const msg =
          err.status === 401 ? '401: Sesión no válida. Inicia sesión nuevamente.' :
          err.status === 403 ? '403: Acceso denegado. Solo ADMIN puede ver usuarios.' :
          err.status === 404 ? '404: Endpoint /api/users no existe.' :
          err?.error?.message || 'Error cargando usuarios.';

        this.showPageMessage('error', 'Error', msg);
        this.pushToast('error', 'Error', msg);
      }
    });
  }

  /* =========================
     CREAR USUARIO
     ========================= */
  onCreate() {
    this.clearMessages();

    if (this.formCreate.invalid) {
      this.formCreate.markAllAsTouched();
      this.pushToast('error', 'Validación', 'Revisa los campos obligatorios antes de continuar.');
      return;
    }

    const v = this.formCreate.value;
    const dto = {
      username: (v.username || '').trim(),
      email:    (v.email || '').trim(),
      password: v.password || '',
      rol:      (v.rol || 'USER') as 'USER' | 'ADMIN',
    };

    this.loading.set(true);

    this.api.adminCreateUser(dto).subscribe({
      next: (res) => {
        this.loading.set(false);

        const msg = res?.message || 'Usuario creado correctamente.';
        this.showPageMessage('success', 'Creación', msg);
        this.pushToast('success', 'Creación', msg);

        this.formCreate.reset({ rol: 'USER' as any });
        this.cargarUsuarios();
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        const msg = err?.error?.message || 'Error creando usuario.';
        this.showPageMessage('error', 'Creación', msg);
        this.pushToast('error', 'Creación', msg);
      }
    });
  }

  /* =========================
     PREPARAR RESET
     ========================= */
  setResetFromUser(u: UserListItemDto) {
    this.formReset.patchValue({ userId: u.id });
    this.selectedUser.set(u);
  }

  /* =========================
     RESET PASSWORD
     ========================= */
  onReset() {
    this.clearMessages();

    if (this.formReset.invalid) {
      this.formReset.markAllAsTouched();
      this.pushToast('error', 'Validación', 'Completa el ID de usuario y la contraseña temporal.');
      return;
    }

    const v = this.formReset.value;
    const dto = {
      userId: v.userId as number,
      newPassword: v.tempPassword as string,
      mustChangePassword: this.mustChangePassword(), // si tu backend no lo soporta, puedes quitarlo
    };

    this.loading.set(true);

    this.api.adminResetPassword(dto as any).subscribe({
      next: (res) => {
        this.loading.set(false);

        const msg = res?.message || 'Contraseña temporal asignada.';
        this.showPageMessage('success', 'Reset', msg);
        this.pushToast('success', 'Reset', msg);

        this.formReset.reset();
        this.mustChangePassword.set(true);
        this.cargarUsuarios();
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        const msg = err?.error?.message || 'Error reseteando contraseña.';
        this.showPageMessage('error', 'Reset', msg);
        this.pushToast('error', 'Reset', msg);
      }
    });
  }

  /* =========================
     ELIMINAR (confirmación)
     ========================= */
  askDelete(u: UserListItemDto) {
    this.pendingDelete.set(u);
    this.confirmDeleteOpen.set(true);
  }

  closeConfirmDelete() {
    this.confirmDeleteOpen.set(false);
    this.pendingDelete.set(null);
  }

  confirmDelete() {
    const u = this.pendingDelete();
    if (!u) return;

    this.pushToast('info', 'Eliminar', 'Aún no está implementado el endpoint de eliminación.');
    this.showPageMessage('info', 'Eliminar', 'Endpoint de eliminación no implementado. (UI lista)');
    this.closeConfirmDelete();

    this.loading.set(true);
this.api.adminDeleteUser(u.id).subscribe({
  next: (res) => {
    this.loading.set(false);
    const msg = res?.message || 'Usuario eliminado correctamente.';
    this.pushToast('success', 'Eliminar', msg);
    this.showPageMessage('success', 'Eliminar', msg);
    this.closeConfirmDelete();
    this.cargarUsuarios();
  },
  error: (err) => {
    this.loading.set(false);
console.log('DELETE ERROR =>', err);
  const msg = err?.error?.message || `Error eliminando usuario. Status: ${err?.status}`;
    this.pushToast('error', 'Eliminar', msg);
    this.showPageMessage('error', 'Eliminar', msg);
  }
});

  }
}
