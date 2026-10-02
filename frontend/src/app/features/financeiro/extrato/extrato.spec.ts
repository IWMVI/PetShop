import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideTestEnv } from '../../../../testing/providers';
import { FinanceiroApi } from '../../../core/api';
import { ExtratoResponse, LancamentoFinanceiro } from '../../../core/models';
import { ConfirmacaoService } from '../../../shared/confirmacao.service';
import { formatarMoeda } from '../../../shared/format';
import { ToastService } from '../../../shared/toast/toast.service';
import { Extrato } from './extrato';

const lancamentoManual: LancamentoFinanceiro = {
  id: 1,
  tipo: 'ENTRADA',
  categoria: 'VENDA_PRODUTO',
  descricao: 'Venda de ração',
  valor: 80,
  status: 'PAGO',
  dataVencimento: null,
  dataPagamento: '2026-01-15T10:30:00',
  vencido: false,
  pagamentoId: null,
};

const lancamentoAutomatico: LancamentoFinanceiro = {
  id: 2,
  tipo: 'ENTRADA',
  categoria: 'PAGAMENTO_SERVICO',
  descricao: 'Pagamento do agendamento #50',
  valor: 150,
  status: 'PAGO',
  dataVencimento: null,
  dataPagamento: '2026-01-16T09:00:00',
  vencido: false,
  pagamentoId: 9,
};

function resposta(itens: LancamentoFinanceiro[]): ExtratoResponse {
  return {
    lancamentos: { itens, pagina: 0, tamanho: 10, total: itens.length, totalPaginas: 1 },
    totalEntradas: 230,
    totalSaidas: 50,
    saldo: 180,
  };
}

describe('Extrato', () => {
  let fixture: ComponentFixture<Extrato>;
  let api: jest.Mocked<Pick<FinanceiroApi, 'extrato' | 'cancelar'>>;
  let confirmacao: { perigo: jest.Mock };
  const toast = { sucesso: jest.fn(), erro: jest.fn() };

  beforeEach(async () => {
    toast.sucesso.mockReset();
    toast.erro.mockReset();
    api = {
      extrato: jest.fn().mockReturnValue(of(resposta([lancamentoManual, lancamentoAutomatico]))),
      cancelar: jest.fn().mockReturnValue(of(undefined)),
    };
    confirmacao = { perigo: jest.fn((c) => c.aoConfirmar()) };

    await TestBed.configureTestingModule({
      imports: [Extrato],
      providers: [
        provideTestEnv(),
        { provide: FinanceiroApi, useValue: api },
        { provide: ConfirmacaoService, useValue: confirmacao },
        { provide: ToastService, useValue: toast },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(Extrato);
    fixture.detectChanges();
  });

  const el = () => fixture.nativeElement as HTMLElement;
  const comp = () =>
    fixture.componentInstance as unknown as {
      filtros: Extrato['filtros'];
      irPara: (i: number) => void;
      confirmarCancelamento: (l: LancamentoFinanceiro) => void;
    };

  it('carrega o extrato com os filtros padrão (sem período, tipo ou categoria)', () => {
    expect(api.extrato).toHaveBeenCalledWith({
      inicio: undefined,
      fim: undefined,
      tipo: undefined,
      categoria: undefined,
      pagina: 0,
      tamanho: 10,
    });
  });

  it('mostra os totais do período', () => {
    expect(el().textContent).toContain(formatarMoeda(230));
    expect(el().textContent).toContain(formatarMoeda(50));
    expect(el().textContent).toContain(formatarMoeda(180));
  });

  it('recarrega voltando para a primeira página ao mudar um filtro', () => {
    comp().irPara(2);
    expect(api.extrato).toHaveBeenCalledWith(expect.objectContaining({ pagina: 2 }));

    comp().filtros.controls.tipo.setValue('SAIDA');

    expect(api.extrato).toHaveBeenLastCalledWith(
      expect.objectContaining({ tipo: 'SAIDA', pagina: 0 }),
    );
  });

  it('esconde as ações de um lançamento gerado automaticamente por um pagamento', () => {
    expect(el().textContent).toContain('Gerado automaticamente');
    // Só o lançamento manual (id 1) tem o menu de ações; o automático (id 2) não tem.
    expect(el().querySelectorAll('[aria-label="Ações"]').length).toBe(1);
  });

  it('cancela o lançamento manual após confirmação e recarrega a lista', () => {
    comp().confirmarCancelamento(lancamentoManual);

    expect(confirmacao.perigo).toHaveBeenCalled();
    expect(api.cancelar).toHaveBeenCalledWith(1);
    expect(toast.sucesso).toHaveBeenCalledWith('Lançamento cancelado.');
    expect(api.extrato).toHaveBeenCalledTimes(2);
  });
});
