import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, RegistroResponseDto } from '../../services/api';

type PageResp<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

@Component({
  selector: 'app-buscar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './buscar.html',
  styleUrls: ['./buscar.css']
})
export class BuscarComponent {
  cedula = '';
  loading = false;
  error: string | null = null;
  tried = false;

  reg: RegistroResponseDto | null = null;

  // ✅ lista paginada
  pageData: PageResp<RegistroResponseDto> | null = null;
  page = 0;
  size = 100;

  constructor(private api: ApiService) {}

  consultar() {
    this.tried = true;
    this.error = null;
    this.reg = null;
    this.pageData = null;

    const c = (this.cedula || '').trim();

    if (!c) {
      // si no hay cédula, lista (como quieres)
      this.listar(true);
      return;
    }

    this.loading = true;
    this.api.getRegistro(c).subscribe({
      next: (res) => {
        this.loading = false;
        this.reg = res || null;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'No se encontró la cédula o ocurrió un error.';
      }
    });
  }

  listar(reset = true) {
    this.tried = true;
    this.error = null;
    this.reg = null;

    if (reset) this.page = 0;

    this.loading = true;
    this.api.listRegistros(this.page, this.size, '').subscribe({
      next: (p: any) => {
        this.loading = false;
        this.pageData = p;
      },
      error: () => {
        this.loading = false;
        this.error = 'No fue posible listar registros.';
      }
    });
  }

  prev() {
    if (!this.pageData || this.page <= 0) return;
    this.page--;
    this.listar(false);
  }

  next() {
    if (!this.pageData || this.page + 1 >= this.pageData.totalPages) return;
    this.page++;
    this.listar(false);
  }

  verDetalle(ced: string) {
    this.cedula = ced;
    this.pageData = null;
    this.consultar();
  }

  limpiar() {
    this.cedula = '';
    this.reg = null;
    this.pageData = null;
    this.error = null;
    this.tried = false;
  }
}
