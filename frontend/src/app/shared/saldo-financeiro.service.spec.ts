import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { FinanceiroApi } from '../core/api';
import { SaldoContas } from '../core/models';
import { SaldoFinanceiroService } from './saldo-financeiro.service';

describe('SaldoFinanceiroService', () => {
  let api: jest.Mocked<Pick<FinanceiroApi, 'saldoContas'>>;

  const saldoPagar: SaldoContas = { totalPendente: 500, totalVencido: 100 };
  const saldoReceber: SaldoContas = { totalPendente: 300, totalVencido: 0 };

  beforeEach(() => {
    api = {
      saldoContas: jest.fn((tipo) => of(tipo === 'SAIDA' ? saldoPagar : saldoReceber)),
    };
    TestBed.configureTestingModule({
      providers: [{ provide: FinanceiroApi, useValue: api }],
    });
  });

  it('começa sem saldo carregado', () => {
    const servico = TestBed.inject(SaldoFinanceiroService);
    expect(servico.aPagar()).toBeNull();
    expect(servico.aReceber()).toBeNull();
  });

  it('carrega os saldos de contas a pagar (SAIDA) e a receber (ENTRADA)', () => {
    const servico = TestBed.inject(SaldoFinanceiroService);

    servico.carregar();

    expect(api.saldoContas).toHaveBeenCalledWith('SAIDA');
    expect(api.saldoContas).toHaveBeenCalledWith('ENTRADA');
    expect(servico.aPagar()).toEqual(saldoPagar);
    expect(servico.aReceber()).toEqual(saldoReceber);
  });
});
