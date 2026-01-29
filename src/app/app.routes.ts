import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { MapaComponent } from './mapa/mapa.component';
import { RegistroFormComponent } from './registro/registro-form/registro-form';
import { RegistroUpdateComponent } from './registro/registro-update/registro-update';
import { LogsComponent } from './logs/logs/logs';
import { BuscarComponent } from './buscar/buscar/buscar';
import { EstadisticasComponent } from './estadisticas/estadisticas';
import { AdminUsuariosComponent } from './admin-usuarios/admin-usuarios';
import { ChangePasswordComponent } from './change-password/change-password';

import { authGuard } from './auth-guard';
import { adminGuard } from './admin-guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },

  // públicas
  { path: 'login', component: LoginComponent },

  // protegidas
  { path: 'cambiar-clave', component: ChangePasswordComponent, canActivate: [authGuard] },
  { path: 'mapa', component: MapaComponent, canActivate: [authGuard] },
  { path: 'registro/crear', component: RegistroFormComponent, canActivate: [authGuard] },
  { path: 'registro/editar', component: RegistroUpdateComponent, canActivate: [authGuard] },
  { path: 'estadisticas', component: EstadisticasComponent, canActivate: [authGuard] },
  { path: 'logs', component: LogsComponent, canActivate: [authGuard] },
  { path: 'buscar', component: BuscarComponent, canActivate: [authGuard] },

  // admin
  { path: 'admin/usuarios', component: AdminUsuariosComponent, canActivate: [authGuard, adminGuard] },

  // fallback
  { path: '**', redirectTo: 'login' },
];
