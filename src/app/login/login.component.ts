import { Component, OnInit, signal, effect } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { HttpClient, HttpClientModule, HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, HttpClientModule],
  template: `
    <main class="page">
      <!-- Fondo (blanco) + overlays azules/verdes -->
      <div class="bg">
        <div class="bg__gradient"></div>
        <div class="bg__grid"></div>
        <div class="bg__orb orb--1"></div>
        <div class="bg__orb orb--2"></div>
      </div>

      <section class="card" [class.card--loading]="loading()">
        <div class="scanline"></div>

        <div class="card__header">
          <img src="/images/logo-nimaima.svg" alt="Logo" class="logo" />
          <h1 class="title">PLAGEOPA</h1>
          <p class="subtitle">
            {{ isSignup() ? 'Crear cuenta' : 'Iniciar sesión' }} · Plataforma de Georreferenciación
          </p>
        </div>

        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form" novalidate>
          <label class="field">
            <span class="field__label">Usuario</span>
            <div class="field__control">
              <span class="field__icon" aria-hidden="true">👤</span>
              <input formControlName="username" type="text" autocomplete="username" required />
            </div>
            @if (submitted && form.controls['username'].invalid) {
              <small class="err">El usuario es obligatorio</small>
            }
          </label>

          @if (isSignup()) {
            <label class="field">
              <span class="field__label">Correo</span>
              <div class="field__control">
                <span class="field__icon" aria-hidden="true">✉️</span>
                <input formControlName="email" type="email" autocomplete="email" required />
              </div>
              @if (submitted && form.controls['email'].invalid) {
                <small class="err">Correo válido obligatorio</small>
              }
            </label>
          }

          <label class="field">
            <span class="field__label">Contraseña</span>
            <div class="field__control">
              <span class="field__icon" aria-hidden="true">🔒</span>
              <input formControlName="password" type="password" autocomplete="current-password" required />
            </div>
            @if (submitted && form.controls['password'].invalid) {
              <small class="err">La contraseña es obligatoria</small>
            }
          </label>

          <div class="row">
            <label class="check">
              <input type="checkbox" formControlName="remember" />
              <span>Recordarme</span>
            </label>
            <a href="#" class="link" (click)="onToggleClick($event)">
              {{ isSignup() ? '¿Ya tienes cuenta? Inicia sesión' : '¿No tienes cuenta? Crear una' }}
            </a>
          </div>

          <button class="btn" type="submit" [disabled]="form.invalid || loading()">
            <span class="btn__glow"></span>
            @if (!loading()) {
              <span class="btn__text">
                {{ isSignup() ? 'Crear cuenta' : 'Ingresar' }}
              </span>
            } @else {
              <span class="spinner" aria-hidden="true"></span> Procesando…
            }
          </button>
        </form>

        <p class="foot">© {{ year }} Alcaldía de Nimaima · Grupo TIC</p>
      </section>
    </main>
  `,
  styles: [`
    :host {
      --bg: #ffffff;                 /* Fondo blanco */
      --grid: rgba(14, 165, 233, .08);  /* azul 500 con transparencia */
      --grid-2: rgba(34, 197, 94, .06); /* verde 500 suave */
      --primary: #0ea5e9;            /* azul */
      --primary-600: #0284c7;
      --accent: #22c55e;             /* verde */
      --accent-600: #16a34a;
      --text: #0b1220;
      --muted: #6b7280;
      --surface: rgba(255,255,255,0.72);
      --border: rgba(14, 165, 233, .25);
      --ring: rgba(34, 197, 94, .35);
      --shadow: 0 20px 60px rgba(2,132,199,.15), 0 8px 24px rgba(34,197,94,.10);
      --radius: 20px;
      --duration: .35s;
      --cubic: cubic-bezier(.2,.8,.2,1);
      display:block;
      color: var(--text);
    }

    .page{
      min-height: 100dvh;
      background: var(--bg);
      display:grid;
      place-items:center;
      overflow:hidden;
      position:relative;
      padding: clamp(16px, 3vw, 28px);
      isolation:isolate;
    }

    /* Fondo: grid sutil + brillos en azul/verde */
    .bg{ position:absolute; inset:0; z-index:0; pointer-events:none; }
    .bg__gradient{
      position:absolute; inset:0;
      background:
        radial-gradient(60vw 60vw at 80% -10%, rgba(14,165,233,.10), transparent 60%),
        radial-gradient(50vw 50vw at -10% 90%, rgba(34,197,94,.10), transparent 55%);
      mix-blend-mode: screen;
    }
    .bg__grid{
      position:absolute; inset:0;
      background-image:
        linear-gradient(0deg, var(--grid) 1px, transparent 1px),
        linear-gradient(90deg, var(--grid-2) 1px, transparent 1px);
      background-size: 28px 28px;
      mask-image: radial-gradient(80% 80% at 50% 50%, #000 65%, transparent 100%);
      animation: gridFloat 16s linear infinite;
      opacity:.8;
    }
    @keyframes gridFloat { to { background-position: 0 28px, 28px 0; } }

    .bg__orb{
      position:absolute; width:38vmin; height:38vmin; border-radius:50%;
      filter: blur(22px);
      opacity:.35;
      transform: translateZ(0);
      animation: orb 22s var(--cubic) infinite alternate;
    }
    .orb--1{ background: radial-gradient(circle, rgba(14,165,233,.55), transparent 60%); top:10%; left:5%; }
    .orb--2{ background: radial-gradient(circle, rgba(34,197,94,.55), transparent 60%); bottom:8%; right:8%; animation-delay: .6s; }
    @keyframes orb {
      0%   { transform: translate3d(0,0,0) scale(1); }
      100% { transform: translate3d(4vmin,-3vmin,0) scale(1.08); }
    }

    /* Card futurista */
    .card{
      position:relative;
      z-index:1;
      width:min(92vw, 720px);
      border-radius: var(--radius);
      background: linear-gradient(180deg, var(--surface), rgba(255,255,255,.9));
      border:1px solid var(--border);
      box-shadow: var(--shadow);
      backdrop-filter: blur(8px);
      padding: clamp(18px, 3vw, 28px);
      overflow:hidden;
    }
    .card--loading{ pointer-events:none; opacity:.95; }

    .scanline{
      position:absolute; inset:0;
      background:
        linear-gradient(180deg, transparent 0%, rgba(14,165,233,.08) 2%, transparent 4%) 0 0/100% 16px;
      mix-blend-mode: soft-light;
      animation: scan 6s linear infinite;
      opacity:.4;
      pointer-events:none;
    }
    @keyframes scan { to { background-position-y: 16px; } }

    .card__header{
      display:grid; place-items:center; text-align:center; gap:10px; margin-bottom: 18px;
    }
    .logo{ width:64px; height:64px; filter: drop-shadow(0 4px 10px rgba(14,165,233,.35)); }
    .title{
      margin:0;
      font-size: clamp(24px, 4vw, 34px);
      letter-spacing:.4px;
      font-weight: 800;
      background: linear-gradient(90deg, var(--primary), var(--accent));
      -webkit-background-clip: text; background-clip:text; color: transparent;
      text-shadow: 0 0 24px rgba(14,165,233,.25);
    }
    .subtitle{
      margin:0; color: var(--muted);
      font-size: clamp(12px, 1.8vw, 14px);
    }

    .form{ display:grid; gap: 14px; }

    /* Campos */
    .field{ display:grid; gap:8px; }
    .field__label{
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing:.12em;
      color: #0b7bb3;
      font-weight: 700;
    }
    .field__control{
      position:relative;
      display:grid;
      grid-template-columns: 40px 1fr;
      align-items:center;
      border:1px solid color-mix(in oklab, var(--primary) 35%, transparent);
      background: #fff;
      border-radius: 14px;
      transition: border-color var(--duration) var(--cubic), box-shadow var(--duration) var(--cubic), transform var(--duration) var(--cubic);
      box-shadow: 0 2px 0 rgba(14,165,233,.12) inset, 0 0 0 0 rgba(34,197,94,.0);
    }
    .field__control:focus-within{
      border-color: color-mix(in oklab, var(--accent) 55%, var(--primary) 45%);
      box-shadow: 0 0 0 4px var(--ring);
      transform: translateY(-1px);
    }
    .field__icon{
      display:grid; place-items:center;
      font-size: 18px;
      opacity:.9;
      color: var(--primary-600);
    }
    input{
      border:0; outline:0; padding: 14px 14px 14px 6px; font-size: 15px; background: transparent; color: var(--text);
    }
    input::placeholder{ color: #a3aab5; }

    .err{
      color:#b91c1c; font-size:12px;
      padding-left: 6px;
    }

    .row{
      display:flex; align-items:center; justify-content: space-between; gap:12px; margin-top: 2px;
    }
    .check{
      display:flex; align-items:center; gap:8px; font-size: 13px; color: var(--muted);
      user-select:none;
    }
    .check input{
      appearance: none; width: 18px; height: 18px; border-radius: 6px;
      border:1px solid var(--border); background:#fff; display:grid; place-items:center;
      transition: all var(--duration) var(--cubic);
    }
    .check input:checked{
      border-color: var(--accent);
      box-shadow: 0 0 0 4px rgba(34,197,94,.15);
      background:
        radial-gradient(circle at 50% 50%, var(--accent) 0 50%, transparent 55%);
      background-repeat:no-repeat;
    }

    .link{
      color: var(--primary-600);
      text-underline-offset: 2px;
      transition: color var(--duration) var(--cubic), text-shadow var(--duration) var(--cubic);
      font-weight: 600;
    }
    .link:hover{
      color: var(--primary);
      text-shadow: 0 0 12px rgba(14,165,233,.35);
    }

    /* Botón con glow */
    .btn{
      position:relative;
      display:inline-grid; place-items:center;
      width:100%;
      border:0; outline:0;
      padding: 14px 16px;
      border-radius: 14px;
      font-weight: 800;
      letter-spacing:.02em;
      color:#fff;
      background: linear-gradient(90deg, var(--primary) 0%, var(--accent) 100%);
      box-shadow: 0 10px 24px rgba(14,165,233,.28), 0 6px 18px rgba(34,197,94,.22);
      cursor:pointer;
      transition: transform var(--duration) var(--cubic), filter var(--duration) var(--cubic), box-shadow var(--duration) var(--cubic);
      overflow:hidden;
    }
    .btn:hover{ transform: translateY(-1px); filter: brightness(1.03); }
    .btn:active{ transform: translateY(0); filter: brightness(.98); }
    .btn:disabled{
      filter: saturate(.6) brightness(.9); cursor:not-allowed; opacity:.85;
    }
    .btn__glow{
      position:absolute; inset:-1px;
      background:
        radial-gradient(120px 40px at var(--mx, 50%) 0%, rgba(255,255,255,.45), transparent 60%),
        radial-gradient(60px 20px at var(--mx, 50%) 100%, rgba(255,255,255,.25), transparent 60%);
      mix-blend-mode: screen; pointer-events:none;
      transition: background-position var(--duration) linear;
    }
    .btn__text{ position:relative; z-index:1; }

    /* Spinner */
    .spinner{
      display:inline-block;
      width: 16px; height: 16px;
      border-radius: 50%;
      border:2px solid rgba(255,255,255,.45);
      border-top-color: #fff;
      animation: spin .8s linear infinite;
      vertical-align: -3px;
      margin-right: 8px;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    /* Footer */
    .foot{
      margin-top: 14px;
      text-align:center;
      font-size: 12px;
      color: var(--muted);
    }

    /* Interacciones: glow del botón sigue el mouse */
    .btn:hover,
    .btn:focus-visible{
      --mx: 50%;
    }
    .btn{ --mx: 50%; }
    .btn:has(.btn__glow){ position:relative; }
    .btn:focus-visible{
      box-shadow:
        0 0 0 4px rgba(255,255,255,.6),
        0 0 0 8px var(--ring);
    }

    /* Accesibilidad / reduce motion */
    @media (prefers-reduced-motion: reduce){
      .bg__grid, .bg__orb, .scanline, .spinner { animation: none !important; }
      .btn, .field__control { transition: none !important; }
    }

    /* Pequeños */
    @media (max-width: 420px){
      .row{ flex-direction: column; align-items:flex-start; gap:10px; }
    }
  `]
})
export class LoginComponent implements OnInit {
  private readonly API_BASE = 'http://localhost:8080/api';

  year = new Date().getFullYear();
  loading = signal(false);
  submitted = false;
  isSignup = signal(false);

  form!: FormGroup;

  constructor(private fb: FormBuilder, private http: HttpClient) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      username: ['', Validators.required],
      email: [''],
      password: ['', Validators.required],
      remember: [true],
    });

    // Valida dinámicamente el email cuando cambia el modo
    effect(() => {
      const signup = this.isSignup();
      const emailCtrl = this.form.get('email')!;
      if (signup) {
        emailCtrl.setValidators([Validators.required, Validators.email]);
      } else {
        emailCtrl.clearValidators();
        emailCtrl.reset();
      }
      emailCtrl.updateValueAndValidity({ emitEvent: false });
    });
  }

  onToggleClick(ev: Event) {
    ev.preventDefault();
    this.toggleMode();
  }

  toggleMode() {
    this.isSignup.set(!this.isSignup());
    this.submitted = false;
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.form.invalid) return;

    this.loading.set(true);
    const { username, email, password } = this.form.value;

    if (this.isSignup()) {
      this.http.get<{ ok: boolean; message: string }>(
        `${this.API_BASE}/users/create`,
        { params: { username, email, password } }
      ).subscribe({
        next: (res) => {
          this.loading.set(false);
          if (res.ok) {
            alert('Usuario creado correctamente. Ahora puedes iniciar sesión.');
            this.isSignup.set(false);
          } else {
            alert(res.message || 'No se pudo crear el usuario.');
          }
        },
        error: (err: HttpErrorResponse) => {
          this.loading.set(false);
          alert(err.error?.message || 'Error creando usuario');
        }
      });
    } else {
      this.http.get<{ ok: boolean; message: string; user?: any }>(
        `${this.API_BASE}/users/login`,
        { params: { username, password } }
      ).subscribe({
        next: (res) => {
          this.loading.set(false);
          if (res.ok && res.user) {
            alert(`Bienvenido, ${res.user.username}`);
            // TODO: guardar token/session si el backend lo provee
          } else {
            alert(res.message || 'Usuario/contraseña inválidos');
          }
        },
        error: (err: HttpErrorResponse) => {
          this.loading.set(false);
          alert(err.error?.message || 'Credenciales inválidas');
        }
      });
    }
  }
}
