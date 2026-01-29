import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, RegistroResponseDto } from '../../services/api';


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

  constructor(private api: ApiService) {}

  consultar() {
    this.tried = true;
    this.error = null;
    this.reg = null;

    if (!this.cedula?.trim()) return;

    this.loading = true;
    this.api.getRegistro(this.cedula.trim()).subscribe({
      next: (res) => {
        this.loading = false;
        this.reg = res || null;
      },
      error: (err) => {
  this.loading = false;
  this.error =
    err?.error?.message ||
    'No se encontró la cédula o ocurrió un error.';
}
    });
  }
}
