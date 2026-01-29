import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';

import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { CredentialsInterceptor } from './interceptor/Credentials-interceptor';
import { Auth401Interceptor } from './interceptor/auth401-interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes), 

    provideHttpClient(withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: CredentialsInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: Auth401Interceptor, multi: true },
  ],
};
