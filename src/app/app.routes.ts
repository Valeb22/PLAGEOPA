import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { MapaComponent } from './mapa/mapa.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginComponent },
  { path: 'mapa', component: MapaComponent },
  { path: '**', redirectTo: 'login' },
];
