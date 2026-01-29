import { CommonModule } from '@angular/common';
import { Component, AfterViewInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { Chart, registerables } from 'chart.js';
import { ApiService } from '../services/api';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';

Chart.register(...registerables);

type KV = { key: string; value: number };

const CHART_BLUE = '#2F5FD7';
const CHART_ORANGE = '#fcb318ff';
const CHART_GRAY = '#94A3B8';

function toKV(obj: any): KV[] {
  if (!obj) return [];
  return Object.entries(obj).map(([key, value]) => ({
    key: String(key),
    value: Number(value) || 0
  }));
}

@Component({
  selector: 'app-estadisticas',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './estadisticas.html',
  styleUrls: ['./estadisticas.css']
})
export class EstadisticasComponent implements AfterViewInit, OnDestroy {

  @ViewChild('barAreaCultivo') barAreaCultivo?: ElementRef<HTMLCanvasElement>;
  @ViewChild('barCountCultivo') barCountCultivo?: ElementRef<HTMLCanvasElement>;
  @ViewChild('pieGenero') pieGenero?: ElementRef<HTMLCanvasElement>;
  @ViewChild('pieAsociacion') pieAsociacion?: ElementRef<HTMLCanvasElement>;
  @ViewChild('pdfArea') pdfArea?: ElementRef<HTMLDivElement>;

  loaded = false;
  exporting = false;

  exportDate = '';

  kpiFincas = 0;
  kpiAreaTotal = 0;
  kpiPromArea = 0;
  kpiCultivosUnicos = 0;
  kpiAreaCultivada = 0;
  kpiUsoSueloPct = 0;

  insights: string[] = [];
  private charts: Chart[] = [];

  constructor(private api: ApiService) {}

  ngAfterViewInit(): void {
    this.api.getStatsReport().subscribe({
      next: (s: any) => {
        this.kpiFincas = s.totalFincas ?? 0;
        this.kpiAreaTotal = s.areaTotalFincas ?? 0;
        this.kpiPromArea = s.areaPromedioPorFinca ?? 0;
        this.kpiCultivosUnicos = s.cultivosUnicos ?? 0;
        this.kpiAreaCultivada = s.areaTotalCultivada ?? 0;
        this.kpiUsoSueloPct = s.usoSueloPct ?? 0;

        this.loaded = true;
        this.exportDate = new Date().toLocaleString('es-CO');

        requestAnimationFrame(() => {
          this.destroyCharts();

          this.makeBar(this.barAreaCultivo, toKV(s.topCultivosArea), 'Área (ha)');
          this.makeBar(this.barCountCultivo, toKV(s.topCultivosFincas), 'Fincas');

          this.makePie(this.pieGenero, toKV(s.generoProductores), 'Género');
          this.makePie(this.pieAsociacion, toKV(s.asociacionProductores), 'Asociación');

          this.insights = this.buildInsights(s);
        });
      },
      error: (e) => {
        console.error(e);
        this.loaded = false;
      }
    });
  }

  async exportarPdf() {
    if (this.exporting) return;
    this.exporting = true;

    try {
      this.exportDate = new Date().toLocaleString('es-CO');
      await new Promise(r => setTimeout(r, 50));

      const el = this.pdfArea?.nativeElement;
      if (!el) return;

      const canvas = await html2canvas(el, {
        scale: 2,
        useCORS: true,
        backgroundColor: '#ffffff',
        scrollX: 0,
        scrollY: -window.scrollY
      });

      const imgData = canvas.toDataURL('image/png');

      const pdf = new jsPDF('p', 'mm', 'a4');
      const pageWidth = 210;
      const pageHeight = 297;

      const margin = 8;
      const usableWidth = pageWidth - margin * 2;
      const usableHeight = pageHeight - margin * 2;

      const imgWidth = usableWidth;
      const imgHeight = (canvas.height * imgWidth) / canvas.width;

      let heightLeft = imgHeight;
      let position = margin;

      pdf.addImage(imgData, 'PNG', margin, position, imgWidth, imgHeight);
      heightLeft -= usableHeight;

      while (heightLeft > 0) {
        pdf.addPage();
        position = margin - (imgHeight - heightLeft);
        pdf.addImage(imgData, 'PNG', margin, position, imgWidth, imgHeight);
        heightLeft -= usableHeight;
      }

      pdf.save('Estadisticas_PLAGEOPA.pdf');
    } finally {
      this.exporting = false;
    }
  }

  private buildInsights(s: any): string[] {
    const ins: string[] = [];

    const topArea = toKV(s.topCultivosArea).sort((a, b) => b.value - a.value);
    if (topArea.length) {
      const top = topArea[0];
      const denom = (s.areaTotalCultivada ?? this.kpiAreaCultivada) || 0;
      const pct = denom ? (top.value / denom) * 100 : 0;
      ins.push(`Cultivo líder por área: "${top.key}" con ${top.value.toFixed(2)} ha (${pct.toFixed(1)}% del área cultivada).`);
    }

    const topCount = toKV(s.topCultivosFincas).sort((a, b) => b.value - a.value);
    if (topCount.length) {
      const top = topCount[0];
      const pct = this.kpiFincas ? (top.value / this.kpiFincas) * 100 : 0;
      ins.push(`Cultivo más frecuente: "${top.key}" en ${top.value} fincas (~${pct.toFixed(1)}% de las fincas).`);
    }

    const uso = Number(s.usoSueloPct ?? 0);
    if (Number.isFinite(uso)) {
      ins.push(`Intensidad de uso del suelo: ${uso.toFixed(1)}% (área cultivada / área total de fincas).`);
    }

    return ins.length ? ins : ['No hay suficientes datos para generar insights automáticos.'];
  }

  private makeBar(el?: ElementRef<HTMLCanvasElement>, data: KV[] = [], yLabel = '') {
    const canvas = el?.nativeElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const labels = data.map(d => d.key);
    const values = data.map(d => d.value);

    const chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: yLabel,
          data: values,
          backgroundColor: CHART_BLUE,
          borderRadius: 6,
          maxBarThickness: 36
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false }, tooltip: { enabled: true } },
        scales: { y: { beginAtZero: true } }
      }
    });

    this.charts.push(chart);
  }

  private makePie(el?: ElementRef<HTMLCanvasElement>, data: KV[] = [], label = '') {
    const canvas = el?.nativeElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const labels = data.map(d => d.key);
    const values = data.map(d => d.value);

    const chart = new Chart(ctx, {
      type: 'pie',
      data: {
        labels,
        datasets: [{
          label,
          data: values,
          backgroundColor: [CHART_BLUE, CHART_ORANGE, CHART_GRAY]
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom' },
          tooltip: { enabled: true }
        }
      }
    });

    this.charts.push(chart);
  }

  private destroyCharts() {
    for (const c of this.charts) c.destroy();
    this.charts = [];
  }

  ngOnDestroy(): void {
    this.destroyCharts();
  }
}
