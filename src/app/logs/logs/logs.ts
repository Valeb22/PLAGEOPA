import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api';
import { FormsModule } from '@angular/forms';
import { saveBlob } from '../../utils/save-blob';

@Component({
  selector: 'app-logs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './logs.html',
  styleUrls: ['./logs.css'],
})
export class LogsComponent implements OnInit {
  start = '';
  end = '';
  items: any[] = [];
  loading = false;

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    const today = new Date().toISOString().slice(0, 10); // YYYY-MM-DD
    this.start = today;
    this.end = today;
    this.load();
  }

  load() {
    if (!this.start || !this.end) return;

    this.loading = true;
    this.items = [];

    this.api.getLogs(this.start, this.end).subscribe({
      next: (res: any) => {
        const list = Array.isArray(res) ? res : (res?.items ?? []);
        this.items = Array.isArray(list) ? list : [];
        this.loading = false;
      },
      error: () => {
        this.items = [];
        this.loading = false;
      },
    });
  }

  exportarExcel() {
    if (!this.start || !this.end) return;

    this.loading = true;

    this.api.downloadLogsExcel(this.start, this.end).subscribe({
      next: (blob) => {
        saveBlob(blob, 'logs.xlsx');
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        alert('No se pudo exportar logs (excel).');
      }
    });
  }

  badgeClass(op: any): string {
    const v = String(op ?? '').toUpperCase().trim();
    if (v.includes('INS') || v.includes('INSERT') || v.includes('CREA')) return 'badge--ins';
    if (v.includes('UPD') || v.includes('UPDATE') || v.includes('ACTUA') || v.includes('MODI')) return 'badge--upd';
    if (v.includes('DEL') || v.includes('DELETE') || v.includes('ELIM')) return 'badge--del';
    return 'badge--unk';
  }
}

