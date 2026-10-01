import { DestroyRef, Injectable, inject, signal } from '@angular/core';

/** Largura abaixo da qual a aplicação usa o layout de celular (mesmo corte do "md" do NG-ZORRO). */
export const LARGURA_CELULAR = 768;

/** Informa, de forma reativa, se a tela atual é de celular. */
@Injectable({ providedIn: 'root' })
export class TelaService {
  private readonly consulta = window.matchMedia(`(max-width: ${LARGURA_CELULAR - 0.02}px)`);
  private readonly _celular = signal(this.consulta.matches);

  /** true em telas com menos de 768px de largura. */
  readonly celular = this._celular.asReadonly();

  constructor() {
    const aoMudar = (e: MediaQueryListEvent) => this._celular.set(e.matches);
    this.consulta.addEventListener('change', aoMudar);
    inject(DestroyRef).onDestroy(() => this.consulta.removeEventListener('change', aoMudar));
  }
}
