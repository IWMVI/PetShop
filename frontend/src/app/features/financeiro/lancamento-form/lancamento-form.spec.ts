import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { provideTestEnv } from '../../../../testing/providers';
import { FinanceiroApi } from '../../../core/api';
import { LancamentoFinanceiro } from '../../../core/models';
import { ToastService } from '../../../shared/toast/toast.service';
import { LancamentoForm } from './lancamento-form';

const lancamentoManual: LancamentoFinanceiro = {
  id: 5,
  tipo: 'SAIDA',
  categoria: 'ALUGUEL',
  descricao: 'Aluguel de outubro',
  valor: 2000,
  data: '2026-01-10T08:00:00',
  pagamentoId: null,
};

describe('LancamentoForm', () => {
  let fixture: ComponentFixture<LancamentoForm>;
  let api: jest.Mocked<Pick<FinanceiroApi, 'buscar' | 'criar' | 'atualizar'>>;
  const toast = { sucesso: jest.fn(), erro: jest.fn() };

  beforeEach(async () => {
    toast.sucesso.mockReset();
    toast.erro.mockReset();
    api = {
      buscar: jest.fn(),
      criar: jest.fn().mockReturnValue(of({ id: 1 })),
      atualizar: jest.fn().mockReturnValue(of({ id: 1 })),
    };
    await TestBed.configureTestingModule({
      imports: [LancamentoForm],
      providers: [
        provideTestEnv(),
        { provide: FinanceiroApi, useValue: api },
        { provide: ToastService, useValue: toast },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(LancamentoForm);
    jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
  });

  const el = () => fixture.nativeElement as HTMLElement;
  const form = () =>
    (fixture.componentInstance as unknown as { form: LancamentoForm['form'] }).form;
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
      tipo: 'ENTRADA',
      categoria: 'VENDA_PRODUTO',
      descricao: 'Venda de ração',
      valor: 80,
      data: new Date(2026, 0, 15, 10, 30),
      ...overrides,
    });

  it('não envia formulário inválido e destaca os campos obrigatórios', () => {
    enviar();
    expect(api.criar).not.toHaveBeenCalled();
    expect(el().querySelectorAll('.ant-form-item-has-error').length).toBe(5);
  });

  it('filtra as opções de categoria conforme o tipo escolhido', () => {
    form().controls.tipo.setValue('ENTRADA');
    fixture.detectChanges();
    expect(
      comp()
        .categorias()
        .map((c) => c.valor),
    ).toEqual(['PAGAMENTO_SERVICO', 'VENDA_PRODUTO', 'OUTRA_RECEITA']);

    form().controls.tipo.setValue('SAIDA');
    fixture.detectChanges();
    expect(
      comp()
        .categorias()
        .map((c) => c.valor),
    ).toEqual(['ALUGUEL', 'SALARIO', 'FORNECEDOR', 'MANUTENCAO', 'IMPOSTO', 'OUTRA_DESPESA']);
  });

  it('limpa a categoria escolhida quando ela deixa de ser compatível com o novo tipo', () => {
    form().controls.tipo.setValue('ENTRADA');
    form().controls.categoria.setValue('VENDA_PRODUTO');

    form().controls.tipo.setValue('SAIDA');

    expect(form().controls.categoria.value).toBeNull();
  });

  it('cadastra o lançamento e volta para o extrato', () => {
    preencher();
    enviar();

    expect(api.criar).toHaveBeenCalledWith({
      tipo: 'ENTRADA',
      categoria: 'VENDA_PRODUTO',
      descricao: 'Venda de ração',
      valor: 80,
      data: '2026-01-15T10:30:00',
    });
    expect(toast.sucesso).toHaveBeenCalledWith('Lançamento cadastrado.');
    expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith(['/financeiro']);
  });

  it('mostra o erro da API e permanece no formulário', () => {
    api.criar.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { mensagem: 'Categoria incompatível com o tipo informado.' },
          }),
      ),
    );
    preencher();
    enviar();

    expect(toast.erro).toHaveBeenCalledWith('Categoria incompatível com o tipo informado.');
    expect(TestBed.inject(Router).navigate).not.toHaveBeenCalled();
  });

  it('carrega o lançamento existente para edição e atualiza', () => {
    api.buscar.mockReturnValue(of(lancamentoManual));
    fixture = TestBed.createComponent(LancamentoForm);
    jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.componentRef.setInput('id', 5);
    fixture.detectChanges();

    expect(form().value.descricao).toBe('Aluguel de outubro');

    enviar();
    expect(api.atualizar).toHaveBeenCalledWith(
      5,
      expect.objectContaining({ descricao: 'Aluguel de outubro', tipo: 'SAIDA' }),
    );
    expect(toast.sucesso).toHaveBeenCalledWith('Lançamento atualizado.');
  });

  it('bloqueia o formulário quando o lançamento foi gerado automaticamente por um pagamento', () => {
    api.buscar.mockReturnValue(of({ ...lancamentoManual, pagamentoId: 9 }));
    fixture = TestBed.createComponent(LancamentoForm);
    jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.componentRef.setInput('id', 5);
    fixture.detectChanges();

    expect(comp().bloqueado()).toBe(true);
    expect(form().disabled).toBe(true);
    expect(el().querySelector('nz-alert')).toBeTruthy();
    expect(el().querySelector('button[type="submit"]')).toHaveProperty('disabled', true);

    enviar();
    expect(api.atualizar).not.toHaveBeenCalled();
  });
});
