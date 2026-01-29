import { CommonModule } from '@angular/common';
import { Component, OnDestroy } from '@angular/core';
import { LeafletDirective } from '@bluehalo/ngx-leaflet';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import * as L from 'leaflet';
import type { Feature, FeatureCollection, Geometry, GeoJsonProperties } from 'geojson';

/* ---------------- Helpers de color (veredas) ---------------- */
const PALETTE = ['#a5d1fdff', '#cc8de6ff', '#eb9cbdff', '#8ee9a5ff'];
let paletteIdx = 0;
function nextPaletteColor(): string {
  const c = PALETTE[paletteIdx % PALETTE.length];
  paletteIdx++;
  return c;
}

/* ---------------- Helpers tooltip ---------------- */
function fmt(v: unknown, fallback = '—'): string {
  if (v === null || v === undefined) return fallback;
  const s = String(v).trim();
  return s.length ? s : fallback;
}

function dedupeCultivos(cultivos: any[]) {
  const map = new Map<string, number>();
  for (const c of (cultivos ?? [])) {
    const key = `${(c.nombre ?? '').trim()}|${(c.variedad ?? '').trim()}`;
    const prev = map.get(key) ?? 0;
    map.set(key, prev + Number(c.area ?? 0));
  }
  return Array.from(map.entries()).map(([key, area]) => {
    const [nombre, variedad] = key.split('|');
    return { nombre: fmt(nombre, ''), variedad: fmt(variedad, ''), area };
  });
}

function buildTooltipHTML(props: any): string {
  const cultivos = dedupeCultivos(props?.cultivos ?? []);
  const correo = props?.productor_correo ?? '';

  let html = `
    <div style="font-size:13px; line-height:1.25">
      <div><b>Cédula:</b> ${fmt(props?.productor_cedula)}</div>
      <div><b>Nombre productor:</b> ${fmt(props?.productor_nombre)}</div>
      <div><b>Género:</b> ${fmt(props?.productor_genero)}</div>
      <div><b>Pertenece a asociación de productores?</b> ${props?.productor_pertenece_asociacion ? 'Sí' : 'No'}</div>
      <div><b>Distribución de la finca (ha):</b> ${fmt(props?.area_total)}</div>
      <div><b>Actividad económica:</b> ${fmt(props?.tipo_actividad)}</div>
  `;

  if (cultivos.length) {
    html += `<hr style="margin:8px 0">`;
    cultivos.forEach((c, i) => {
      const etiqueta = [c.nombre, c.variedad && c.variedad !== '—' ? `· ${c.variedad}` : '']
        .filter(Boolean)
        .join(' ');
      html += `<div><b>Cultivo ${i + 1}:</b> ${etiqueta} · ${fmt(c.area)} ha</div>`;
    });
  }

  if (correo && String(correo).trim().length) {
    html += `<div style="margin-top:6px"><b>Correo:</b> ${fmt(correo)}</div>`;
  }

  html += `</div>`;
  return html;
}

/* ---------------- Normalización tildes/case ---------------- */
function norm(s: any): string {
  return String(s ?? '')
    .trim()
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '');
}

function eqNorm(a: any, b: any): boolean {
  const na = norm(a);
  const nb = norm(b);
  if (!na || !nb) return false;
  return na === nb;
}

function includesNorm(haystack: any, needle: any): boolean {
  const h = norm(haystack);
  const n = norm(needle);
  if (!n) return true;
  return h.includes(n);
}

@Component({
  selector: 'app-mapa',
  standalone: true,
  imports: [CommonModule, LeafletDirective, HttpClientModule, FormsModule],
  templateUrl: './mapa.html',
  styleUrls: ['./mapa.css']
})
export class MapaComponent implements OnDestroy {
  private map!: L.Map;
  private veredasLayer?: L.GeoJSON;
  private fincasLayer?: L.GeoJSON;

  private readonly API = 'http://localhost:8081';

  // filtros
  fCedula = '';
  fCultivo = '';
  resultCount: number | null = null;

  // dataset completo (cache)
  private fincasAll!: FeatureCollection<Geometry>;

  // autocomplete
  showCultivoSug = false;
  cultivoSug: string[] = [];
  private cultivoUniverse: string[] = [];

  options: L.MapOptions = {
    center: L.latLng(5.0993, -74.4235),
    zoom: 13,
    preferCanvas: true,
    layers: [
      L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
        attribution:
          '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> · <a href="https://www.carto.com/">CARTO</a>',
        subdomains: 'abcd',
        maxZoom: 19
      })
    ]
  };

  constructor(private http: HttpClient) {}

  onMapReady(map: L.Map) {
    this.map = map;

    this.map.createPane('veredasPane');
    this.map.getPane('veredasPane')!.style.zIndex = '410';

    this.map.createPane('fincasPane');
    this.map.getPane('fincasPane')!.style.zIndex = '420';

    this.map.createPane('plageopaTooltipPane');
    this.map.getPane('plageopaTooltipPane')!.style.zIndex = '460';

    setTimeout(() => map.invalidateSize(), 0);

    this.cargarVeredas();
    this.cargarFincas();
  }

  private cargarVeredas() {
    const url = `${this.API}/api/veredas`;
    this.http.get<FeatureCollection<Geometry>>(url).subscribe({
      next: (fc) => {
        if (!fc || fc.type !== 'FeatureCollection' || !fc.features?.length) return;

        this.veredasLayer = L.geoJSON(fc, {
          pane: 'veredasPane',
          style: (_feat?: Feature<Geometry, GeoJsonProperties>) => {
            const color = nextPaletteColor();
            return { color, weight: 2, fillOpacity: 0.25, interactive: false } as L.PathOptions;
          }
        }).addTo(this.map);

        this.veredasLayer.bringToBack();
        const bounds = (this.veredasLayer as any).getBounds?.();
        if (bounds && bounds.isValid()) this.map.fitBounds(bounds, { padding: [20, 20] });
      },
      error: (err) => console.error('Error cargando veredas:', err)
    });
  }

  private cargarFincas() {
    const url = `${this.API}/api/geo/fincas`;
    this.http.get<FeatureCollection<Geometry>>(url).subscribe({
      next: (fc) => {
        if (!fc || fc.type !== 'FeatureCollection') return;

        this.fincasAll = fc;
        this.buildCultivoUniverse(fc);

        this.renderFincas(fc);
        this.resultCount = fc.features?.length ?? 0;
      },
      error: (err) => console.error('Error cargando fincas:', err)
    });
  }

  consultar() {
    if (!this.fincasAll) return;

    const cedNeedle = String(this.fCedula ?? '').replace(/\D/g, '');
    const cultivoNeedle = String(this.fCultivo ?? '').trim();

    const filtered: FeatureCollection<Geometry> = {
      type: 'FeatureCollection',
      features: (this.fincasAll.features ?? []).filter((f: any) => {
        const p = f.properties ?? {};

        if (cedNeedle) {
          const ced = String(p.productor_cedula ?? '').replace(/\D/g, '');
          if (!ced.includes(cedNeedle)) return false;
        }

        if (cultivoNeedle) {
          const cults = Array.isArray(p.cultivos) ? p.cultivos : [];
          const has = cults.some((c: any) => eqNorm(c?.nombre, cultivoNeedle));
          if (!has) return false;
        }

        return true;
      })
    };

    this.resultCount = filtered.features.length;
    this.renderFincas(filtered);

    if (filtered.features.length) {
      const bounds = (this.fincasLayer as any)?.getBounds?.();
      if (bounds && bounds.isValid()) this.map.fitBounds(bounds, { padding: [20, 20] });
    }
  }

  limpiar() {
    this.fCedula = '';
    this.fCultivo = '';
    this.resultCount = this.fincasAll?.features?.length ?? null;
    if (this.fincasAll) this.renderFincas(this.fincasAll);
  }

  private renderFincas(fc: FeatureCollection<Geometry>) {
    if (this.fincasLayer) {
      this.fincasLayer.remove();
      this.fincasLayer = undefined;
    }

    if (!fc.features?.length) return;

    this.fincasLayer = L.geoJSON(fc, {
      pane: 'fincasPane',
      pointToLayer: (_feat, latlng) =>
        L.circleMarker(latlng, {
          pane: 'fincasPane',
          radius: 4,
          weight: 2,
          color: '#f7f422ff',     
          fillColor: '#f51c1cff', 
          fillOpacity: 0.9
        }),
      onEachFeature: (feature, layer) => {
        const props: any = (feature as any).properties ?? {};
        const html = buildTooltipHTML(props);

        (layer as any).bindTooltip(html, {
          pane: 'plageopaTooltipPane',
          direction: 'top',
          opacity: 0.98,
          sticky: true,
          offset: L.point(0, -10),
          className: 'plageopa-tip'
        });

        (layer as L.Layer)
          .on('mouseover', (e) => (e.target as any).openTooltip?.())
          .on('mouseout',  (e) => (e.target as any).closeTooltip?.());
      }
    }).addTo(this.map);

    this.fincasLayer.bringToFront();
  }

  private buildCultivoUniverse(fc: FeatureCollection<Geometry>) {
    const set = new Map<string, string>(); // norm -> original
    for (const f of (fc.features ?? []) as any[]) {
      const p = f.properties ?? {};
      const cults = Array.isArray(p.cultivos) ? p.cultivos : [];
      for (const c of cults) {
        const raw = String(c?.nombre ?? '').trim();
        if (!raw) continue;
        const k = norm(raw);
        if (!set.has(k)) set.set(k, raw);
      }
    }
    this.cultivoUniverse = Array.from(set.entries())
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([, v]) => v);
  }

  onCultivoTyping() {
    this.showCultivoSug = true;
    this.updateCultivoSuggestions();
  }

  updateCultivoSuggestions() {
    const term = norm(this.fCultivo);
    if (!term) {
      this.cultivoSug = this.cultivoUniverse.slice(0, 10);
      return;
    }
    this.cultivoSug = this.cultivoUniverse
      .filter(c => includesNorm(c, term))
      .sort((a, b) => {
        const sa = norm(a).startsWith(term) ? 0 : 1;
        const sb = norm(b).startsWith(term) ? 0 : 1;
        return sa - sb || norm(a).indexOf(term) - norm(b).indexOf(term);
      })
      .slice(0, 10);
  }

  pickCultivoSuggestion(s: string) {
    this.fCultivo = s;
    this.showCultivoSug = false;
    this.cultivoSug = [];
  }

  onCultivoBlur() {
    setTimeout(() => {
      this.showCultivoSug = false;
      this.cultivoSug = [];
    }, 120);
  }

  trackByString = (_: number, v: string) => v;

  ngOnDestroy(): void {}
}
