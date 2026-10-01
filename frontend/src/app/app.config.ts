import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import localePt from '@angular/common/locales/pt';
import {
  ApplicationConfig,
  LOCALE_ID,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideNzConfig } from 'ng-zorro-antd/core/config';
import { ptBR } from 'date-fns/locale';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NZ_DATE_LOCALE, provideNzI18n, pt_BR } from 'ng-zorro-antd/i18n';

import { routes } from './app.routes';
import { provideAppIcons } from './shared/icons';

registerLocaleData(localePt);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(),
    provideAnimationsAsync(),
    provideNzI18n(pt_BR),
    // Tema via variáveis CSS (ng-zorro-antd.variable.css); a paleta completa é derivada da cor primária.
    provideNzConfig({
      theme: { primaryColor: '#f5b800' },
      message: { nzTop: 72, nzMaxStack: 3 },
    }),
    // Usa date-fns para formatar e interpretar datas digitadas no nz-date-picker (dd/MM/yyyy).
    { provide: NZ_DATE_LOCALE, useValue: ptBR },
    provideAppIcons(),
    // O NzModalService não é providedIn: 'root'; registrado aqui para o ConfirmacaoService.
    NzModalService,
    { provide: LOCALE_ID, useValue: 'pt-BR' },
  ],
};
