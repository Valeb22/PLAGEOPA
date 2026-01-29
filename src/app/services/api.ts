import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

// src/app/services/api.ts
export interface CultivoDto {
  id?: number;
  nombreCultivo: string;
  variedad?: string | null;
  area: number | null;
}
export interface ProductorDto {
  cedula: string;
  nombre: string;
  telefono?: string | null;
  genero: 'Femenino' | 'Masculino';
  perteneceAsociacion: boolean;
  nombreAsociacion?: string | null;
}
export interface UserListItemDto {
  id: number;
  username: string;
  email: string;
  rol: string; // "USER" | "ADMIN"
  mustChangePassword?: boolean;
}
export interface FincaDto {
  areaTotal: number | null;
  tipoActividad: string | null;
  lon: number | null;
  lat: number | null;
  veredaCodigo?: string | null;
  globalid?: string | null; // para escoger finca si hay varias
}
export interface RegistroCreateDto { productor: ProductorDto; finca: FincaDto; cultivos: CultivoDto[]; }

export interface ProductorUpdateDto { nombre?: string | null; telefono?: string | null; perteneceAsociacion?: boolean | null; nombreAsociacion?: string | null; }
export interface FincaUpdateDto { areaTotal?: number | null; tipoActividad?: string | null; lon?: number | null; lat?: number | null; veredaCodigo?: string | null; globalid?: string | null; }

export interface CultivoUpsertDto { id?: number | null; nombreCultivo?: string; variedad?: string | null; area?: number | null; }
export interface RegistroUpdateDto {
  productor?: ProductorUpdateDto | null;
  finca?: FincaUpdateDto | null;
  cultivosUpsert?: CultivoUpsertDto[] | null;
  cultivosDeleteIds?: number[] | null;
}

export interface CultivoGetDto { id: number; nombreCultivo: string; variedad?: string | null; area: number | null; }
export interface RegistroResponseDto {
  productor: {
    cedula: string; nombre: string; telefono?: string | null;
    genero: 'Femenino' | 'Masculino';
    perteneceAsociacion: boolean; nombreAsociacion?: string | null;
  };
  finca?: {
    areaTotal?: number | null; tipoActividad?: string | null;
    lon?: number | null; lat?: number | null;
    veredaCodigo?: string | null; globalid?: string | null;
  } | null;
  cultivos: CultivoGetDto[];
}


@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly API_BASE = 'http://localhost:8081/api';
  constructor(private http: HttpClient) {}

  private headers(): HttpHeaders {
    const uid = localStorage.getItem('userId');
    let h = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (uid) h = h.set('X-User-Id', uid);
    return h;
  }

createUser(dto: { username: string; email: string; password: string; rol?: string }) {
  return this.http.post(`${this.API_BASE}/users/create`, dto, {
    headers: this.headers(),
    withCredentials: true
  });
}

login(dto: { username: string; password: string }) {
  return this.http.post(`${this.API_BASE}/users/login`, dto, {
    headers: this.headers(),
    withCredentials: true
  });
}



logout() {
  return this.http.post(`${this.API_BASE}/users/logout`, {}, {
    headers: this.headers(),
    withCredentials: true
  });
}

listUsers(): Observable<UserListItemDto[]> {
    return this.http.get<UserListItemDto[]>(`${this.API_BASE}/users`, {
      headers: this.headers(),
      withCredentials: true,
    });
  }

  adminCreateUser(dto: { username: string; email: string; password: string; rol: 'USER' | 'ADMIN' }) {
    return this.http.post<any>(`${this.API_BASE}/users/create`, dto, {
      headers: this.headers(),
      withCredentials: true,
    });
  }

adminResetPassword(dto: { userId: number; newPassword: string }) {
  return this.http.post<any>(`${this.API_BASE}/users/reset-password`, dto, {
    headers: this.headers(),
    withCredentials: true,
  });
}
adminChangePassword(dto: { userId: number; currentPassword: string; newPassword: string }) {
  }
resetPassword(dto: { userId: number; newPassword: string }) {
  return this.http.post(`${this.API_BASE}/users/reset-password`, dto, {
    headers: this.headers(),
    withCredentials: true
  });
}

changePassword(dto: { userId: number; currentPassword: string; newPassword: string }) {
  return this.http.post(`${this.API_BASE}/users/change-password`, dto, {
    headers: this.headers(),
    withCredentials: true
  });
}


  actualizarRegistro(cedula: string, dto: RegistroUpdateDto): Observable<any> {
    return this.http.patch(`${this.API_BASE}/registro/${cedula}`, dto, { headers: this.headers() });
  }
  eliminarCultivo(idCultivo: number): Observable<void> {
    return this.http.delete<void>(`${this.API_BASE}/registro/cultivos/${idCultivo}`, { headers: this.headers() });
  }
  eliminarProductor(cedula: string): Observable<void> {
    return this.http.delete<void>(`${this.API_BASE}/registro/productores/${cedula}`, { headers: this.headers() });
  }
getRegistro(cedula: string) {
  return this.http.get<RegistroResponseDto>(`${this.API_BASE}/registro/${cedula}`, { headers: this.headers() });
}

crearRegistro(dto: RegistroCreateDto): Observable<any> {
    return this.http.post(`${this.API_BASE}/registro`, dto, { headers: this.headers() });
  }
getLogs(from?: string, to?: string) {
  const params: any = {};
  if (from) params.from = from;
  if (to)   params.to   = to;
  return this.http.get<any[]>(`${this.API_BASE}/logs`, {
    params,
    headers: this.headers()
  }
);
}

getStatsReport() {
  return this.http.get<any>(`${this.API_BASE}/reports/stats`, {
    headers: this.headers(),
    withCredentials: true
  });
}

downloadStatsPdf() {
  return this.http.get(`${this.API_BASE}/reports/stats.pdf`, {
    responseType: 'blob',
    headers: this.headers(),
    withCredentials: true
  });
}

downloadLogsExcel(from?: string, to?: string) {
  let params: any = {};
  if (from) params.from = from;
  if (to) params.to = to;

  return this.http.get(`${this.API_BASE}/reports/logs.xlsx`, {
    responseType: 'blob',
    params,
    headers: this.headers(),
    withCredentials: true
  });
}

adminDeleteUser(id: number) {
  return this.http.delete<any>(`${this.API_BASE}/users/${id}`, {
    headers: this.headers(),
    withCredentials: true,
  });
}

}

