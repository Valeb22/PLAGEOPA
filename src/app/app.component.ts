import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ApiService } from './services/api';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="topbar" *ngIf="showShell()">
      <div class="topbar__content">
        <img src="/images/logo-nimaima.svg" alt="Logo institucional" class="topbar__logo" />
        <span class="topbar__title">ALCALDÍA DE NIMAIMA</span>
      </div>
    </header>

    <nav class="tabs" *ngIf="showShell()" aria-label="Navegación principal">
      <a routerLink="/mapa" routerLinkActive="active" class="tab" title="Mapa" aria-label="Mapa">
        <img src="assets/nav/mapa.png" alt="Mapa" />
        <span>Mapa</span>
      </a>

      <a routerLink="/registro/crear" routerLinkActive="active" class="tab" title="Crear" aria-label="Crear">
        <img src="assets/nav/crear.jpg" alt="Crear" />
        <span>Crear</span>
      </a>
<a routerLink="/carga-masiva" routerLinkActive="active" class="tab" title="Carga masiva" aria-label="Carga masiva">
  <img src="assets/nav/cargar.png" alt="Carga masiva" />
  <span>Carga masiva</span>
</a>

      <a
        routerLink="/registro/editar"
        routerLinkActive="active"
        class="tab tab--wide"
        title="Editar / Eliminar"
        aria-label="Editar y Eliminar">
        <img src="assets/nav/updateanddelete2.png" alt="Editar o eliminar" class="tab-img-large" />
        <span>Editar/Eliminar</span>
      </a>

      <a routerLink="/buscar" routerLinkActive="active" class="tab" title="Buscar" aria-label="Buscar">
        <img src="assets/nav/buscar.png" alt="Buscar" />
        <span>Buscar</span>
      </a>

      <a routerLink="/logs" routerLinkActive="active" class="tab" title="Logs" aria-label="Logs">
        <img src="assets/nav/log.png" alt="Logs" />
        <span>Logs</span>
      </a>

      <a routerLink="/estadisticas" routerLinkActive="active" class="tab" title="Estadísticas" aria-label="Estadísticas">
        <img src="assets/nav/estadistica.png" alt="Estadísticas" />
        <span>Estadísticas</span>
      </a>

      <a
        *ngIf="isAdmin()"
        routerLink="/admin/usuarios"
        routerLinkActive="active"
        class="tab"
        title="Gestión de usuarios"
        aria-label="Gestión de usuarios">
        <img src="assets/nav/usuarios.png" alt="Usuarios" />
        <span>Usuarios</span>
      </a>

      <span class="spacer"></span>

      <button class="logout" (click)="logout()" aria-label="Salir">Salir</button>
    </nav>

    <router-outlet></router-outlet>
  `,
  styles: [`
    .topbar{
      background:#2F5FD7; color:#fff; height:55px; display:flex; align-items:center; box-shadow:0 2px 6px rgba(0,0,0,.12);
    }
    .topbar__content{ width:min(1200px,92vw); margin:0 auto; display:flex; align-items:center; gap:12px; }
    .topbar__logo{ height:40px; width:auto; object-fit:contain; }
    .topbar__title{ font-weight:600; letter-spacing:.4px; }

    .tabs{
      background:#f8fafc; border-bottom:1px solid #e2e8f0;
      display:flex; gap:10px; align-items:center; padding:8px 16px;
    }
    .tab{
      --ring: rgba(34,197,94,.25);
      display:inline-flex; align-items:center; gap:8px;
      padding:8px 10px; border-radius:12px; text-decoration:none; color:#0f172a;
      border:1px solid transparent; transition: all .2s ease;
    }
    .tab img{
      width:28px; height:28px; object-fit:cover; border-radius:8px;
      box-shadow: 0 1px 2px rgba(0,0,0,.06);
    }
    .tab span{
      font-size:14px; font-weight:600;
    }
    .tab:hover{
      background:#eef2ff; border-color:#c7d2fe; transform: translateY(-1px);
      box-shadow: 0 0 0 4px var(--ring);
    }
    .tab.active{
      background:#e2e8f0; border-color:#cbd5e1;
    }
    .tabs .spacer{ flex:1; }
    .logout{
      background:#ef4444; color:#fff; border:none; padding:8px 12px; border-radius:10px; cursor:pointer; font-weight:700;
      transition: filter .2s ease, transform .2s ease;
    }
    .logout:hover{ filter:brightness(.95); transform: translateY(-1px); }

    .tab img.tab-img-large{
      width:73px;
      height:30px;
      border-radius:8px;
      box-shadow: 0 2px 6px rgba(0,0,0,.12);
      transform: translateY(-1px);
    }

    .tab.tab--wide{ padding:10px 12px; }

    @media (max-width: 720px){
      .tab span{ display:none; }
      .tab{ padding:8px; }
      .tab.tab--wide{ padding:8px; }
      .tab img.tab-img-large{ width:44px; height:44px; }
    }
  `]
})
export class AppComponent {
  private router = inject(Router);
  private api = inject(ApiService);

  showShell() {
    // oculta barra superior y tabs en pantallas públicas
    return this.router.url !== '/login' && !this.router.url.startsWith('/reset-password');
  }

  isAdmin(): boolean {
    return (localStorage.getItem('role') || '').toUpperCase() === 'ADMIN';
  }

  logout() {
    // si ya tienes backend con sesión, lo ideal es llamarlo
    this.api.logout().subscribe({
      next: () => this.finishLogout(),
      error: () => this.finishLogout()
    });
  }

  private finishLogout() {
    localStorage.removeItem('logged');
    localStorage.removeItem('userId');
    localStorage.removeItem('role');
    this.router.navigateByUrl('/login');
  }
}
