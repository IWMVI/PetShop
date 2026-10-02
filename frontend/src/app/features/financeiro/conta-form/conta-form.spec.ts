import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { provideTestEnv } from '../../../../testing/providers';
import { FinanceiroApi } from '../../../core/api';
import { LancamentoFinanceiro } from '../../../core/models';
import { SaldoFinanceiroService } from '../../../shared/saldo-financeiro.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { ContaForm } from './conta-form';

const contaPendente: LancamentoFinanceiro = {
  id: 5,
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

describe('ContaForm', () => {
  let fixture: ComponentFixture<ContaForm>;
  let api: jest.Mocked<Pick<FinanceiroApi, 'buscar' | 'registrarConta' | 'atualizarConta'>>;
  let saldoFinanceiro: { carregar: jest.Mock };
  const toast = { sucesso: jest.fn(), erro: jest.fn() };

  beforeEach(async () => {
    toast.sucesso.mockReset();
    toast.erro.mockReset();
    api = {
      buscar: jest.fn(),
      registrarConta: jest.fn().mockReturnValue(of({ id: 1 })),
      atualizarConta: jest.fn().mockReturnValue(of({ id: 1 })),
    };
    saldoFinanceiro = { carregar: jest.fn() };
    await TestBed.configureTestingModule({
      imports: [ContaForm],
      providers: [
        provideTestEnv(),
        { provide: FinanceiroApi, useValue: api },
        { provide: SaldoFinanceiroService, useValue: saldoFinanceiro },
        { provide: ToastService, useValue: toast },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(ContaForm);
    jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  });

  const el = () => fixture.nativeElement as HTMLElement;
  const form = () => (fixture.componentInstance as unknown as { form: ContaForm['form'] }).form;
  const comp = () =>
    fixture.componentInstance as unknown as {
      categorias: () => { valor: string; rotulo: string }[];
      bloqueado: () => boolean;
    };
  const enviar = () => {
    el().querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  };
  const preencher = (overrides: Partial<ReturnType<typeof form>['value']> = {}) =>
    form().setValue({
      categoria: 'ALUGUEL',
      descricao: 'Aluguel de outubro',
      valor: 2000,
      dataVencimento: new Date(2026, 9, 10),
      ...overrides,
    });
  const criar = (tipo: 'SAIDA' | 'ENTRADA' = 'SAIDA') => {
    fixture.componentRef.setInput('tipo', tipo);
    fixture.detectChanges();
  };

  it('não envia formulário inválido e destaca os campos obrigatórios', () => {
    criar();
    enviar();
    expect(api.registrarConta).not.toHaveBeenCalled();
    expect(el().querySelectorAll('.ant-form-item-has-error').length).toBe(4);
  });

  it('filtra as opções de categoria conforme o tipo fixo da rota', () => {
    criar('SAIDA');
    expect(
      comp()
        .categorias()
        .map((c) => c.valor),
    ).toEqual(['ALUGUEL', 'SALARIO', 'FORNECEDOR', 'MANUTENCAO', 'IMPOSTO', 'OUTRA_DESPESA']);
  });

  it('cadastra uma conta a pagar (tipo SAIDA)', () => {
    criar('SAIDA');
    preencher();
    enviar();

    expect(api.registrarConta).toHaveBeenCalledWith({
      tipo: 'SAIDA',
      categoria: 'ALUGUEL',
      descricao: 'Aluguel de outubro',
      valor: 2000,
      dataVencimento: '2026-10-10T00:00:00',
    });
    expect(toast.sucesso).toHaveBeenCalledWith('Conta cadastrada.');
    expect(saldoFinanceiro.carregar).toHaveBeenCalled();
    expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith(['/financeiro/contas-a-pagar']);
  });

  it('cadastra uma conta a receber (tipo ENTRADA)', () => {
    criar('ENTRADA');
    expect(
      comp()
        .categorias()
        .map((c) => c.valor),
    ).toEqual(['PAGAMENTO_SERVICO', 'VENDA_PRODUTO', 'OUTRA_RECEITA']);

    preencher({ categoria: 'VENDA_PRODUTO', descricao: 'Venda de ração', valor: 80 });
    enviar();

    expect(api.registrarConta).toHaveBeenCalledWith(
      expect.objectContaining({ tipo: 'ENTRADA', categoria: 'VENDA_PRODUTO' }),
    );
    expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith(['/financeiro/contas-a-receber']);
  });

  it('mostra o erro da API e permanece no formulário', () => {
    criar();
    api.registrarConta.mockReturnValue(
      throwError(
        () => new HttpErrorResponse({ status: 400, error: { mensagem: 'Dados inválidos.' } }),
      ),
    );
    preencher();
    enviar();

    expect(toast.erro).toHaveBeenCalledWith('Dados inválidos.');
    expect(TestBed.inject(Router).navigate).not.toHaveBeenCalled();
  });

  it('carrega a conta existente para edição e atualiza', () => {
    api.buscar.mockReturnValue(of(contaPendente));
    fixture.componentRef.setInput('id', 5);
    criar('SAIDA');

    expect(form().value.descricao).toBe('Aluguel de outubro');

    enviar();
    expect(api.atualizarConta).toHaveBeenCalledWith(
      5,
      expect.objectContaining({ tipo: 'SAIDA', descricao: 'Aluguel de outubro' }),
    );
    expect(toast.sucesso).toHaveBeenCalledWith('Conta atualizada.');
  });

  it('bloqueia o formulário quando a conta já não está pendente', () => {
    api.buscar.mockReturnValue(of({ ...contaPendente, status: 'PAGO' }));
    fixture.componentRef.setInput('id', 5);
    criar('SAIDA');

    expect(comp().bloqueado()).toBe(true);
    expect(form().disabled).toBe(true);
    expect(el().querySelector('nz-alert')).toBeTruthy();
    expect(el().querySelector('button[type="submit"]')).toHaveProperty('disabled', true);

    enviar();
    expect(api.atualizarConta).not.toHaveBeenCalled();
  });
});
