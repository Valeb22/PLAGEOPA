import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormArray,
  FormGroup,
} from '@angular/forms';
import { ApiService, RegistroResponseDto } from '../../services/api';

type ToastType = 'success' | 'error' | 'info';
type Toast = { id: string; type: ToastType; title: string; message: string };

@Component({
  selector: 'app-registro-update',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './registro-update.html',
  styleUrls: ['./registro-update.css'],
})
export class RegistroUpdateComponent implements OnInit {
  loading = signal(false);
  submitted = false;

  searchCedula = '';
  reg?: RegistroResponseDto;

  form!: FormGroup;

  // Toasts
  toasts = signal<Toast[]>([]);

  constructor(private fb: FormBuilder, private api: ApiService) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      productor: this.fb.group({
        nombre: [''],
        telefono: [''],
        perteneceAsociacion: [null as boolean | null],
        nombreAsociacion: [''],
      }),
      finca: this.fb.group({
        areaTotal: [null as number | null, [Validators.min(0)]],
        tipoActividad: [''],
        lon: [null as number | null, [Validators.min(-180), Validators.max(180)]],
        lat: [null as number | null, [Validators.min(-90), Validators.max(90)]],
        veredaCodigo: [''],
        globalid: [''],
      }),
      cultivosAgregar: this.fb.array([]),
      cultivosActualizar: this.fb.array([]),
    });
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

  // ---------- Form arrays ----------
  get cultivosAgregar(): FormArray {
    return this.form.get('cultivosAgregar') as FormArray;
  }
  get cultivosActualizar(): FormArray {
    return this.form.get('cultivosActualizar') as FormArray;
  }

  // ---------- Mensajes de error claros ----------
  fieldError(path: string): string | null {
    const c = this.form.get(path);
    if (!c) return null;

    const show = this.submitted || c.touched;
    if (!show || !c.errors) return null;

    if (c.errors['min']) return 'El valor no puede ser negativo.';
    if (c.errors['max']) return 'El valor supera el máximo permitido.';
    return 'Revisa este campo.';
  }

  // ---------- Acciones ----------
  cargarPorCedula() {
    this.submitted = false;

    const ced = String(this.searchCedula ?? '').replace(/\D/g, '');
    if (!ced) {
      this.pushToast('error', 'Validación', 'Ingresa una cédula válida.');
      return;
    }
    this.searchCedula = ced;

    this.loading.set(true);
    this.pushToast('info', 'Consulta', 'Buscando registro…');

    this.api.getRegistro(this.searchCedula).subscribe({
      next: (data) => {
        this.loading.set(false);
        this.reg = data;

        this.cultivosAgregar.clear();
        this.cultivosActualizar.clear();

        this.form.get('productor')?.patchValue({
          nombre: data.productor?.nombre ?? '',
          telefono: data.productor?.telefono ?? '',
          perteneceAsociacion: data.productor?.perteneceAsociacion ?? null,
          nombreAsociacion: data.productor?.nombreAsociacion ?? '',
        });

        this.form.get('finca')?.patchValue({
          areaTotal: data.finca?.areaTotal ?? null,
          tipoActividad: data.finca?.tipoActividad ?? '',
          lon: data.finca?.lon ?? null,
          lat: data.finca?.lat ?? null,
          veredaCodigo: data.finca?.veredaCodigo ?? '',
          globalid: data.finca?.globalid ?? '',
        });

        (data.cultivos || []).forEach((c) => {
          this.cultivosActualizar.push(
            this.fb.group({
              id: [c.id, Validators.required],
              nombreCultivo: [c.nombreCultivo || ''],
              variedad: [c.variedad || ''],
              area: [c.area ?? null, [Validators.min(0)]],
            }),
          );
        });

        this.pushToast(
          'success',
          'Cargado',
          'Datos cargados. Ajusta lo necesario y presiona “Guardar cambios”.'
        );
      },
      error: (err) => {
        this.loading.set(false);
        const msg = err?.error?.error || err?.error?.message || 'No encontrado.';
        this.pushToast('error', 'Error', msg);
      },
    });
  }

  addCultivoNuevo() {
    this.cultivosAgregar.push(
      this.fb.group({
        nombreCultivo: ['', Validators.required],
        variedad: [''],
        area: [0, [Validators.min(0)]],
      }),
    );
    this.pushToast('info', 'Cultivos', 'Se agregó una fila para crear cultivo.');
  }

  removeCultivoNuevo(i: number) {
    this.cultivosAgregar.removeAt(i);
    this.pushToast('info', 'Cultivos', 'Se eliminó la fila del cultivo nuevo.');
  }

  addCultivoActualizar() {
    this.cultivosActualizar.push(
      this.fb.group({
        id: [null, Validators.required],
        nombreCultivo: [''],
        variedad: [''],
        area: [null as number | null, [Validators.min(0)]],
      }),
    );
    this.pushToast('info', 'Cultivos', 'Se agregó una fila para modificar cultivo.');
  }

  removeCultivoActualizar(i: number) {
    this.cultivosActualizar.removeAt(i);
    this.pushToast('info', 'Cultivos', 'Se eliminó la fila del cultivo a modificar.');
  }

  patch() {
    this.submitted = true;

    const ced = String(this.searchCedula ?? '').replace(/\D/g, '');
    if (!ced) {
      this.pushToast('error', 'Validación', 'Ingresa una cédula válida.');
      return;
    }
    this.searchCedula = ced;

    // Validación: si el usuario marcó "Sí" en perteneceAsociacion, exigir nombreAsociacion
    const belongs = this.form.get('productor.perteneceAsociacion')?.value;
    const nombreAso = String(this.form.get('productor.nombreAsociacion')?.value ?? '').trim();
    if (belongs === true && !nombreAso) {
      this.form.get('productor.nombreAsociacion')?.setErrors({ required: true });
      this.pushToast('error', 'Validación', 'Si pertenece a una asociación, debes indicar el nombre.');
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.pushToast('error', 'Validación', 'Revisa los campos marcados.');
      return;
    }

    const v = this.form.value as any;

    // DTO consistente con tu api.ts (veredaCodigo, no codigoVereda)
    const dto = {
      productor: v.productor,
      finca: {
        areaTotal: v.finca.areaTotal,
        tipoActividad: v.finca.tipoActividad,
        lon: v.finca.lon,
        lat: v.finca.lat,
        veredaCodigo: v.finca.veredaCodigo,
        globalid: v.finca.globalid || null,
      },
      cultivosUpsert: [
        ...(this.cultivosActualizar.value ?? []),
        ...(this.cultivosAgregar.value ?? []),
      ],
    };

    this.loading.set(true);
    this.pushToast('info', 'Actualización', 'Guardando cambios…');

    this.api.actualizarRegistro(this.searchCedula, dto).subscribe({
      next: () => {
        this.loading.set(false);
        this.pushToast('success', 'Actualizado', 'Se guardaron los cambios correctamente.');
      },
      error: (err) => {
        this.loading.set(false);
        const msg = err?.error?.error || err?.error?.message || 'Error actualizando.';
        this.pushToast('error', 'Error', msg);
      },
    });
  }

  eliminarCultivo(id: any) {
    const n = Number(id);
    if (!Number.isFinite(n) || n <= 0) {
      this.pushToast('error', 'Validación', 'ID de cultivo inválido.');
      return;
    }
    if (!confirm(`¿Eliminar cultivo ${n}?`)) return;

    this.loading.set(true);
    this.api.eliminarCultivo(n).subscribe({
      next: () => {
        this.loading.set(false);
        this.pushToast('success', 'Eliminado', `Cultivo ${n} eliminado.`);
        // opcional: refrescar lista cargada
        // this.cargarPorCedula();
      },
      error: (err) => {
        this.loading.set(false);
        const msg = err?.error?.error || err?.error?.message || 'Error eliminando cultivo.';
        this.pushToast('error', 'Error', msg);
      },
    });
  }

  eliminarProductor() {
    const ced = String(this.searchCedula ?? '').replace(/\D/g, '');
    if (!ced) {
      this.pushToast('error', 'Validación', 'Ingresa una cédula válida.');
      return;
    }
    this.searchCedula = ced;

    if (!confirm(`¿Eliminar productor ${this.searchCedula} y todos sus datos?`)) return;

    this.loading.set(true);
    this.pushToast('info', 'Eliminación', 'Eliminando productor…');

    this.api.eliminarProductor(this.searchCedula).subscribe({
      next: () => {
        this.loading.set(false);
        this.pushToast('success', 'Eliminado', `Productor ${this.searchCedula} eliminado.`);
        this.reg = undefined;
        this.form.reset({
          productor: { nombre: '', telefono: '', perteneceAsociacion: null, nombreAsociacion: '' },
          finca: { areaTotal: null, tipoActividad: '', lon: null, lat: null, veredaCodigo: '', globalid: '' },
          cultivosAgregar: [],
          cultivosActualizar: [],
        });
        this.cultivosAgregar.clear();
        this.cultivosActualizar.clear();
      },
      error: (err) => {
        this.loading.set(false);
        const msg = err?.error?.error || err?.error?.message || 'Error eliminando productor.';
        this.pushToast('error', 'Error', msg);
      },
    });
  }
}
