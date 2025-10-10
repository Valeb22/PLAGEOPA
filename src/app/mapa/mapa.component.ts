import { Component } from '@angular/core';
import { LeafletDirective } from '@bluehalo/ngx-leaflet';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import * as L from 'leaflet';
import type { Feature, FeatureCollection, Geometry } from 'geojson';

@Component({
  selector: 'app-mapa',
  standalone: true,
  imports: [LeafletDirective, HttpClientModule],
  template: `
    <div
      leaflet
      [leafletOptions]="options"
      (leafletMapReady)="onMapReady($event)"
      style="height: 80vh; width: 100%; display: block;"
    ></div>
  `
})
export class MapaComponent {
  private map!: L.Map;

  options: L.MapOptions = {
    center: L.latLng(5.0993, -74.4235),
    zoom: 14,
    layers: [
      L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
        attribution:
          '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://www.carto.com/">CARTO</a>',
        subdomains: 'abcd',
        maxZoom: 19
      })
    ]
  };

  constructor(private http: HttpClient) {}

  onMapReady(map: L.Map) {
    this.map = map;
    setTimeout(() => map.invalidateSize(), 0);
    this.cargarVeredas(); // ← trae todo desde Spring
  }

  private cargarVeredas() {
    const url = 'http://localhost:8080/api/veredas'; // ajusta si tu backend usa otro host/ruta

    this.http.get<FeatureCollection<Geometry>>(<string>url).subscribe({
      next: (fc) => {
        if (!fc || fc.type !== 'FeatureCollection' || !fc.features?.length) return;

        const layer = L.geoJSON(fc, {
          style: () => ({ color: '#1e88e5', weight: 2, fillOpacity: 0.35 }),
          onEachFeature: (feature: Feature, lyr: L.Layer) => {
            const props = feature.properties as any;
            const cod = props?.codigo_corto ?? '(sin código)';
            (lyr as any).bindPopup(`<b>Vereda</b><br>codigo_corto: ${cod}`);
          }
        }).addTo(this.map);

        const bounds = layer.getBounds?.();
        if (bounds && bounds.isValid()) {
          this.map.fitBounds(bounds, { padding: [20, 20] });
        }
      },
      error: (err) => {
        console.error('Error cargando veredas:', err);
      }
    });
  }
}
