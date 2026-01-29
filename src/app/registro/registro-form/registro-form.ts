import { Component, OnInit, inject, signal } from '@angular/core';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormArray,
  FormGroup
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api';

type ToastType = 'success' | 'error' | 'info';
type Toast = { id: string; type: ToastType; title: string; message: string };

@Component({
  selector: 'app-registro-form',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './registro-form.html',
  styleUrls: ['./registro-form.css']
})
export class RegistroFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);

  loading = signal(false);
  submitted = false;

  // Toasts
  toasts = signal<Toast[]>([]);

  form!: FormGroup;

  ngOnInit(): void {
    this.form = this.fb.group({
      productor: this.fb.group({
        cedula: ['', [Validators.required, Validators.pattern(/^\d{1,10}$/)]],
        nombre: ['', [Validators.required, Validators.minLength(3)]],
        telefono: [''],
        genero: ['Femenino', Validators.required],
        perteneceAsociacion: [false],
        nombreAsociacion: ['']
      }),
      finca: this.fb.group({
        areaTotal: [0, [Validators.required, Validators.min(0)]],
        tipoActividad: ['Agrícola', Validators.required], // ✅ default
        lon: [null, [Validators.required, Validators.min(-180), Validators.max(180)]],
        lat: [null, [Validators.required, Validators.min(-90), Validators.max(90)]],
        veredaCodigo: ['']
      }),
      cultivos: this.fb.array([])
    });

    // opcional: arranca con 1 cultivo por defecto
    if (this.cultivos.length === 0) this.addCultivo();
  }

  // ---------- Toast helpers ----------
  private pushToast(type: ToastType, title: string, message: string) {
    const id = (crypto as any)?.randomUUID?.() ?? String(Date.now() + Math.random());
    const toast: Toast = { id, type, title, message };
    this.toasts.update(list => [toast, ...list].slice(0, 3));
    setTimeout(() => this.dismissToast(id), 5000);
  }

  dismissToast(id: string) {
    this.toasts.update(list => list.filter(t => t.id !== id));
  }

  // ---------- Form helpers ----------
  get cultivos(): FormArray {
    return this.form.get('cultivos') as FormArray;
  }

  addCultivo() {
    this.cultivos.push(
      this.fb.group({
        nombreCultivo: ['', Validators.required],
        variedad: [''],
        area: [0, [Validators.required, Validators.min(0)]]
      })
    );
    this.pushToast('info', 'Cultivos', 'Se agregó una fila para registrar un cultivo.');
  }

  removeCultivo(i: number) {
    this.cultivos.removeAt(i);
    this.pushToast('info', 'Cultivos', 'Se eliminó la fila del cultivo.');
  }

  // Mensajes de error claros por campo
  fieldError(path: string): string | null {
    const c = this.form.get(path);
    if (!c) return null;

    const show = this.submitted || c.touched;
    if (!show || !c.errors) return null;

    if (c.errors['required']) return 'Este campo es obligatorio.';
    if (c.errors['minlength']) {
      const r = c.errors['minlength'];
      return `Debe tener mínimo ${r.requiredLength} caracteres.`;
    }
    if (c.errors['pattern']) return 'Formato inválido. Solo números (máx. 10 dígitos).';
    if (c.errors['min']) return 'El valor no puede ser negativo.';
    if (c.errors['max']) return 'El valor supera el máximo permitido.';
    return 'Revisa este campo.';
  }

  // Reglas extra: si perteneceAsociacion = true, exigir nombreAsociacion
  private validateAsociacion(): boolean {
    const belongs = !!this.form.get('productor.perteneceAsociacion')?.value;
    const nombreAso = String(this.form.get('productor.nombreAsociacion')?.value ?? '').trim();

    if (belongs && !nombreAso) {
      this.form.get('productor.nombreAsociacion')?.setErrors({ required: true });
      this.pushToast('error', 'Validación', 'Si pertenece a una asociación, debes indicar el nombre.');
      return false;
    }
    return true;
  }

  submit() {
    this.submitted = true;

    // limpia toasts anteriores “de ruido” si quieres
    // this.toasts.set([]);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.pushToast('error', 'Validación', 'Revisa los campos marcados en rojo.');
      return;
    }

    if (!this.validateAsociacion()) return;

    // Validación extra: al menos 1 cultivo con nombre
    const hasCultivoValido = (this.cultivos.value ?? []).some((c: any) => String(c?.nombreCultivo ?? '').trim());
    if (!hasCultivoValido) {
      this.pushToast('error', 'Cultivos', 'Debes registrar al menos un cultivo (Nombre obligatorio).');
      return;
    }

    this.loading.set(true);

    this.api.crearRegistro(this.form.value as any).subscribe({
      next: () => {
        this.loading.set(false);

        this.pushToast('success', 'Creado', 'Registro creado exitosamente.');

        // reset con defaults
        this.form.reset({
          productor: {
            cedula: '',
            nombre: '',
            telefono: '',
            genero: 'Femenino',
            perteneceAsociacion: false,
            nombreAsociacion: ''
          },
          finca: {
            areaTotal: 0,
            tipoActividad: 'Agrícola',
            lon: null,
            lat: null,
            veredaCodigo: ''
          },
          cultivos: []
        });

        this.cultivos.clear();
        this.addCultivo(); // deja 1 fila lista
        this.submitted = false;
      },
      error: (err) => {
        this.loading.set(false);

        const msg =
          err?.error?.error ||
          err?.error?.message ||
          'No se pudo crear el registro. Verifica la información e intenta nuevamente.';

        this.pushToast('error', 'Error', msg);
      }
    });
  }
}
