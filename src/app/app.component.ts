import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <header class="topbar">
      <div class="topbar__content">
        <!-- Espacio para tu logo -->
        <img src="/images/logo-nimaima.svg" alt="Logo institucional" class="topbar__logo" />
        <span class="topbar__title">ALCALDÍA DE NIMAIMA</span>
      </div>
    </header>

    <router-outlet></router-outlet>
  `,
  styles: [`
    .topbar{
      background:#2F5FD7; /* gov blue */
      color:#fff;
      height:64px;
      display:flex;
      align-items:center;
      box-shadow:0 2px 6px rgba(0,0,0,.12);
    }
    .topbar__content{
      width:min(1200px,92vw);
      margin:0 auto;
      display:flex;
      align-items:center;
      gap:12px;
    }
    .topbar__logo{ height:40px; width:auto; object-fit:contain; }
    .topbar__title{ font-weight:600; letter-spacing:.4px; }
  `]
})
export class AppComponent {}
