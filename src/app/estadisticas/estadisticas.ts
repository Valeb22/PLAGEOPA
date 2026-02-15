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

// Opcional: embellecer nombres tipo "CAFE" -> "Café"
function prettyCultivoName(raw: any): string {
  const s = String(raw ?? '').trim();
  if (!s) return '';
  const lower = s.toLowerCase();
  const titled = lower.replace(/\b\p{L}/gu, (m) => m.toUpperCase());
  if (titled === 'Cafe') return 'Café';
  return titled.replace(/_/g, ' ');
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

  // ✅ nuevo chart
  @ViewChild('barShareArea') barShareArea?: ElementRef<HTMLCanvasElement>;

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

  // ✅ datos tabla participación
  cultivosShareRows: Array<{
    cultivo: string;
    cultivoPretty: string;
    productores: number;
    productoresPct: number;
    fincas: number;
    fincasPct: number;
    areaHa: number;
    areaPct: number;
  }> = [];

  private charts: Chart[] = [];

  constructor(private api: ApiService) {}

  ngAfterViewInit(): void {
    this.api.getStatsReport().subscribe({
      next: (s: any) => {
        this.kpiFincas = Number(s?.totalFincas ?? 0);
        this.kpiAreaTotal = Number(s?.areaTotalFincas ?? 0);
        this.kpiPromArea = Number(s?.areaPromedioPorFinca ?? 0);
        this.kpiCultivosUnicos = Number(s?.cultivosUnicos ?? 0);
        this.kpiAreaCultivada = Number(s?.areaTotalCultivada ?? 0);
        this.kpiUsoSueloPct = Number(s?.usoSueloPct ?? 0);

        // ✅ construir tabla participación
        this.cultivosShareRows = this.buildShareRows(s);

        this.loaded = true;
        this.exportDate = new Date().toLocaleString('es-CO');

        requestAnimationFrame(() => {
          this.destroyCharts();

          this.makeBar(this.barAreaCultivo, toKV(s?.topCultivosArea), 'Área (ha)');
          this.makeBar(this.barCountCultivo, toKV(s?.topCultivosFincas), 'Fincas');

          this.makePie(this.pieGenero, toKV(s?.generoProductores), 'Género');
          this.makePie(this.pieAsociacion, toKV(s?.asociacionProductores), 'Asociación');

          // ✅ grafica participación por área (top 10)
          this.makeShareAreaBar(this.barShareArea, this.cultivosShareRows.slice(0, 10));

          // ✅ conclusiones cortas
          this.insights = this.buildInsights(s, this.cultivosShareRows);
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

  private buildShareRows(s: any) {
    const shareObj = s?.cultivosShare || {};
    const rows = (Object.values(shareObj) as any[]).map(r => {
      const cultivo = String(r?.cultivo ?? '');
      return {
        cultivo,
        cultivoPretty: prettyCultivoName(cultivo),
        productores: Number(r?.productores ?? 0),
        productoresPct: Number(r?.productoresPct ?? 0),
        fincas: Number(r?.fincas ?? 0),
        fincasPct: Number(r?.fincasPct ?? 0),
        areaHa: Number(r?.areaHa ?? 0),
        areaPct: Number(r?.areaPct ?? 0),
      };
    });

rows.sort((a, b) => b.productoresPct - a.productoresPct);
    return rows;
  }

  // ✅ ahora conclusiones: 3–4 máximo (no lista infinita)
  private buildInsights(s: any, shareRows: any[]): string[] {
    const ins: string[] = [];

    const top = shareRows?.[0];
    if (top) {
      ins.push(`Cultivo líder: ${top.cultivoPretty} con ${top.areaHa.toFixed(2)} ha (${top.areaPct.toFixed(1)}% del área cultivada).`);
      ins.push(`${top.cultivoPretty} está presente en ${top.fincas} fincas (${top.fincasPct.toFixed(1)}% del total de fincas).`);
    }

    const uso = Number(s?.usoSueloPct ?? 0);
    if (Number.isFinite(uso)) {
      ins.push(`Uso del suelo: ${uso.toFixed(1)}% (área cultivada / área total de fincas).`);
    }

    // concentración simple: top 2 o top 3
    const top3 = shareRows.slice(0, 3);
    if (top3.length >= 2) {
      const sum = top3.reduce((acc: number, r: any) => acc + Number(r.areaPct || 0), 0);
      ins.push(`Concentración productiva: los 3 principales cultivos representan ${sum.toFixed(1)}% del área cultivada.`);
    }

    return ins.length ? ins : ['No hay suficientes datos para generar conclusiones.'];
  }

  private makeBar(el?: ElementRef<HTMLCanvasElement>, data: KV[] = [], yLabel = '') {
    const canvas = el?.nativeElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const labels = data.map(d => prettyCultivoName(d.key));
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

  private makeShareAreaBar(el?: ElementRef<HTMLCanvasElement>, rows: any[] = []) {
    const canvas = el?.nativeElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const labels = rows.map(r => r.cultivoPretty);
    const values = rows.map(r => Number(r.areaPct ?? 0)); // % área

    const chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: '% área cultivada',
          data: values,
          backgroundColor: CHART_ORANGE,
          borderRadius: 6,
          maxBarThickness: 36
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false }, tooltip: { enabled: true } },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: (v) => `${v}%`
            }
          }
        }
      }
    });

    this.charts.push(chart);
  }

  private makePie(el?: ElementRef<HTMLCanvasElement>, data: KV[] = [], label = '') {
    const canvas = el?.nativeElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const labels = data.map(d => String(d.key));
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
