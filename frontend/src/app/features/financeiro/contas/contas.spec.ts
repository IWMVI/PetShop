import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { provideTestEnv } from '../../../../testing/providers';
import { FinanceiroApi } from '../../../core/api';
import { LancamentoFinanceiro, SaldoContas } from '../../../core/models';
import { ConfirmacaoService } from '../../../shared/confirmacao.service';
import { formatarMoeda } from '../../../shared/format';
import { SaldoFinanceiroService } from '../../../shared/saldo-financeiro.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { Contas } from './contas';

const contaPendente: LancamentoFinanceiro = {
  id: 1,
  tipo: 'SAIDA',
  categoria: 'ALUGUEL',
  descricao: 'Aluguel de outubro',
  valor: 2000,
  status: 'PENDENTE',
  dataVencimento: '2026-10-10T00:00:00',
  dataPagamento: null,
  vencido: false,
  pagamentoId: null,
};

const contaVencida: LancamentoFinanceiro = {
  id: 2,
  tipo: 'SAIDA',
  categoria: 'FORNECEDOR',
  descricao: 'Ração para revenda',
  valor: 500,
  status: 'PENDENTE',
  dataVencimento: '2026-09-01T00:00:00',
  dataPagamento: null,
  vencido: true,
  pagamentoId: null,
};

const contaPaga: LancamentoFinanceiro = {
  id: 3,
  tipo: 'SAIDA',
  categoria: 'MANUTENCAO',
  descricao: 'Conserto do ar-condicionado',
  valor: 300,
  status: 'PAGO',
  dataVencimento: '2026-08-01T00:00:00',
  dataPagamento: '2026-08-01T10:00:00',
  vencido: false,
  pagamentoId: null,
};

function pagina(itens: LancamentoFinanceiro[]) {
  return { itens, pagina: 0, tamanho: 10, total: itens.length, totalPaginas: 1 };
}

describe('Contas', () => {
  let fixture: ComponentFixture<Contas>;
  let api: jest.Mocked<Pick<FinanceiroApi, 'listarContas' | 'marcarComoPaga' | 'cancelar'>>;
  let confirmacao: { perigo: jest.Mock };
  let saldoFinanceiro: {
    aPagar: () => SaldoContas | null;
    aReceber: () => SaldoContas | null;
    carregar: jest.Mock;
  };
  const toast = { sucesso: jest.fn(), erro: jest.fn() };
  const aPagar = signal<SaldoContas | null>({ totalPendente: 2500, totalVencido: 500 });

  function criarComponente(tipo: 'SAIDA' | 'ENTRADA' = 'SAIDA') {
    fixture = TestBed.createComponent(Contas);
    fixture.componentRef.setInput('tipo', tipo);
    fixture.detectChanges();
  }

  beforeEach(async () => {
    toast.sucesso.mockReset();
    toast.erro.mockReset();
    aPagar.set({ totalPendente: 2500, totalVencido: 500 });
    api = {
      listarContas: jest.fn().mockReturnValue(of(pagina([contaPendente, contaVencida]))),
      marcarComoPaga: jest.fn().mockReturnValue(of(contaPendente)),
      cancelar: jest.fn().mockReturnValue(of(undefined)),
    };
    confirmacao = { perigo: jest.fn((c) => c.aoConfirmar()) };
    saldoFinanceiro = {
      aPagar: () => aPagar(),
      aReceber: () => null,
      carregar: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [Contas],
      providers: [
        provideTestEnv(),
        { provide: FinanceiroApi, useValue: api },
        { provide: ConfirmacaoService, useValue: confirmacao },
        { provide: SaldoFinanceiroService, useValue: saldoFinanceiro },
        { provide: ToastService, useValue: toast },
      ],
    }).compileComponents();
    criarComponente();
  });

  const el = () => fixture.nativeElement as HTMLElement;
  const comp = () =>
    fixture.componentInstance as unknown as {
      filtroStatus: Contas['filtroStatus'];
      irPara: (i: number) => void;
      confirmarPagamento: (l: LancamentoFinanceiro) => void;
      confirmarCancelamento: (l: LancamentoFinanceiro) => void;
    };

  it('carrega as contas pendentes por padrão', () => {
    expect(api.listarContas).toHaveBeenCalledWith('SAIDA', 'PENDENTE', 0, 10);
  });

  it('mostra o título e o resumo de pendente/vencido conforme o tipo', () => {
    expect(el().textContent).toContain('Contas a Pagar');
    expect(el().textContent).toContain(formatarMoeda(2500));
    expect(el().textContent).toContain(formatarMoeda(500));
  });

  it('filtra por status, voltando para a primeira página', () => {
    comp().irPara(2);
    expect(api.listarContas).toHaveBeenCalledWith('SAIDA', 'PENDENTE', 2, 10);

    comp().filtroStatus.setValue('PAGO');
    expect(api.listarContas).toHaveBeenLastCalledWith('SAIDA', 'PAGO', 0, 10);

    comp().filtroStatus.setValue('TODAS');
    expect(api.listarContas).toHaveBeenLastCalledWith('SAIDA', undefined, 0, 10);
  });

  it('destaca a conta vencida com o rótulo "Vencida" em vez de "Pendente"', () => {
    const linhas = Array.from(el().querySelectorAll('tbody tr'));
    const linhaVencida = linhas.find((l) => l.textContent?.includes('Ração para revenda'))!;
    const linhaPendente = linhas.find((l) => l.textContent?.includes('Aluguel de outubro'))!;

    expect(linhaVencida.querySelector('app-status')?.textContent).toContain('Vencida');
    expect(linhaVencida.querySelector('.status-erro')).not.toBeNull();
    expect(linhaPendente.querySelector('app-status')?.textContent).toContain('Pendente');
  });

  it('marca a conta como paga após confirmação, recarrega a lista e atualiza o saldo do menu', () => {
    comp().confirmarPagamento(contaPendente);

    expect(confirmacao.perigo).toHaveBeenCalled();
    expect(api.marcarComoPaga).toHaveBeenCalledWith(1, { dataPagamento: expect.any(String) });
    expect(toast.sucesso).toHaveBeenCalledWith('Conta marcada como paga.');
    expect(api.listarContas).toHaveBeenCalledTimes(2);
    expect(saldoFinanceiro.carregar).toHaveBeenCalled();
  });

  it('usa o rótulo "recebida" para contas do tipo ENTRADA', () => {
    criarComponente('ENTRADA');
    comp().confirmarPagamento(contaPendente);

    expect(toast.sucesso).toHaveBeenCalledWith('Conta marcada como recebida.');
  });

  it('cancela a conta após confirmação, recarrega a lista e atualiza o saldo do menu', () => {
    comp().confirmarCancelamento(contaPendente);

    expect(confirmacao.perigo).toHaveBeenCalled();
    expect(api.cancelar).toHaveBeenCalledWith(1);
    expect(toast.sucesso).toHaveBeenCalledWith('Conta cancelada.');
    expect(api.listarContas).toHaveBeenCalledTimes(2);
    expect(saldoFinanceiro.carregar).toHaveBeenCalled();
  });

  it('esconde as ações de uma conta já paga', () => {
    api.listarContas.mockReturnValue(of(pagina([contaPendente, contaPaga])));
    criarComponente();

    // Só a conta pendente (id 1) tem o menu de ações; a paga (id 3) não tem.
    expect(el().querySelectorAll('[aria-label="Ações"]').length).toBe(1);
    expect(el().textContent).toContain('Sem ações');
  });

  it('esconde as ações de uma conta com pagamentoId preenchido, mesmo que ainda pendente', () => {
    api.listarContas.mockReturnValue(
      of(pagina([contaPendente, { ...contaPendente, id: 4, pagamentoId: 9 }])),
    );
    criarComponente();

    expect(el().querySelectorAll('[aria-label="Ações"]').length).toBe(1);
  });
});
