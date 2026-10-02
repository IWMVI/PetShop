import { Injectable, inject, signal } from '@angular/core';
import { FinanceiroApi } from '../core/api';
import { SaldoContas } from '../core/models';

/**
 * Saldos de contas a pagar e a receber, usados no badge do menu lateral. As telas de
 * Contas chamam `carregar()` de novo após criar, pagar ou cancelar uma conta, para o
 * badge do menu refletir a mudança sem precisar recarregar a página inteira.
 */
@Injectable({ providedIn: 'root' })
export class SaldoFinanceiroService {
  private readonly api = inject(FinanceiroApi);
  private readonly _aPagar = signal<SaldoContas | null>(null);
  private readonly _aReceber = signal<SaldoContas | null>(null);
  readonly aPagar = this._aPagar.asReadonly();
  readonly aReceber = this._aReceber.asReadonly();

  carregar() {
    this.api.saldoContas('SAIDA').subscribe((s) => this._aPagar.set(s));
    this.api.saldoContas('ENTRADA').subscribe((s) => this._aReceber.set(s));
  }
}
