import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ApiService } from '../services/api';
import { saveBlob } from '../utils/save-blob';

type PreviewRow = {
  rowNum: number;
  ok: boolean;
  errors: string[];
  cedula: string;
  nombre: string;
  genero: string;
  perteneceAsociacion: string;
  areaTotal: number;
  tipoActividad: string;
  lon: number;
  lat: number;
  codigoVereda?: string;
  nombreCultivo: string;
  variedad?: string;
  areaCultivo: number;
  globalid?: string;
};

@Component({
  selector: 'app-carga-masiva',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './carga-masiva.html',
  styleUrls: ['./carga-masiva.css']
})
export class CargaMasivaComponent {
  file: File | null = null;

  loading = false;
  preview: { totalRows: number; okRows: number; errorRows: number; rows: PreviewRow[] } | null = null;

  constructor(private api: ApiService) {}

  descargarPlantilla() {
    this.api.downloadTemplate().subscribe({
      next: (blob) => saveBlob(blob, 'PLAGEOPA_Plantilla.xlsx'),
      error: () => alert('No se pudo descargar la plantilla.')
    });
  }

  onFileChange(ev: Event) {
    const input = ev.target as HTMLInputElement;
    this.file = input.files?.[0] ?? null;
    this.preview = null;
  }

  previsualizar() {
    if (!this.file) return;

    this.loading = true;
    this.api.previewImport(this.file, 200).subscribe({
      next: (p) => {
        this.preview = p;
        this.loading = false;
      },
      error: (e) => {
        console.error(e);
        this.loading = false;
        alert('No se pudo previsualizar. Verifica el archivo.');
      }
    });
  }

  importar() {
    if (!this.file) return;
    if (!this.preview) return;

    if (this.preview.errorRows > 0) {
      alert('Hay filas con errores. Corrige el Excel antes de importar.');
      return;
    }

    this.loading = true;
    this.api.applyImport(this.file).subscribe({
      next: (res) => {
        this.loading = false;
        alert(
          `Importación OK.\n` +
          `Filas: ${res.totalRows}\n` +
          `Productores: +${res.insertedProductores} / upd ${res.updatedProductores}\n` +
          `Fincas: +${res.insertedFincas} / upd ${res.updatedFincas}\n` +
          `Cultivos: +${res.insertedCultivos}\n` +
          `Errores: ${res.errorRows}`
        );
      },
      error: (e) => {
        console.error(e);
        this.loading = false;
        alert('Falló la importación.');
      }
    });
  }
}
