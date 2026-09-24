import { EnvironmentProviders, Provider } from '@angular/core';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { provideAppIcons } from '../app/shared/icons';

/** Providers comuns aos testes de componentes (rotas, animações do NG-ZORRO e ícones). */
export function provideTestEnv(): (Provider | EnvironmentProviders)[] {
  return [provideRouter([]), provideNoopAnimations(), provideAppIcons(), NzModalService];
}
