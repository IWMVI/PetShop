import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { provideTestEnv } from '../../../../testing/providers';
import { AgendamentoApi, PagamentoApi } from '../../../core/api';
import { Agendamento, Pagamento } from '../../../core/models';
import { ConfirmacaoService } from '../../../shared/confirmacao.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { AgendamentoDetail } from './agendamento-detail';

const agendamento: Agendamento = {
  id: 50,
  petId: 7,
  dataHora: '2099-12-31T10:00:00',
  observacoes: 'Levar coleira',
  status: 'AGENDADO',
  valorTotal: 150,
  servicos: [{ servicoId: 1, nome: 'Banho', precoCobrado: 150 }],
};

const pagamentoPendente: Pagamento = {
  id: 1,
  agendamentoId: 50,
  valor: 150,
  metodoPagamento: 'PIX',
  status: 'PENDENTE',
  dataPagamento: null,
};

describe('AgendamentoDetail', () => {
  let fixture: ComponentFixture<AgendamentoDetail>;
  let agendamentoApi: jest.Mocked<Pick<AgendamentoApi, 'buscar'>>;
  let pagamentoApi: jest.Mocked<Pick<PagamentoApi, 'listar' | 'atualizarStatus' | 'cancelar'>>;
  let confirmacao: { perigo: jest.Mock };

  beforeEach(async () => {
    agendamentoApi = { buscar: jest.fn().mockReturnValue(of(agendamento)) };
    pagamentoApi = {
      listar: jest.fn().mockReturnValue(of([pagamentoPendente])),
      atualizarStatus: jest.fn().mockReturnValue(of(pagamentoPendente)),
      cancelar: jest.fn().mockReturnValue(of(undefined)),
    };
    confirmacao = { perigo: jest.fn((c) => c.aoConfirmar()) };

    await TestBed.configureTestingModule({
      imports: [AgendamentoDetail],
      providers: [
        provideTestEnv(),
        { provide: AgendamentoApi, useValue: agendamentoApi },
        { provide: PagamentoApi, useValue: pagamentoApi },
        { provide: ConfirmacaoService, useValue: confirmacao },
        { provide: ToastService, useValue: { sucesso: jest.fn(), erro: jest.fn() } },
      ],
    }).compileComponents();
  });

  const criar = () => {
    fixture = TestBed.createComponent(AgendamentoDetail);
    fixture.componentRef.setInput('tutorId', 1);
    fixture.componentRef.setInput('petId', 7);
    fixture.componentRef.setInput('agendamentoId', 50);
    fixture.detectChanges();
  };

  const el = () => fixture.nativeElement as HTMLElement;

  it('carrega o agendamento e os pagamentos ao iniciar', () => {
    criar();

    expect(agendamentoApi.buscar).toHaveBeenCalledWith(7, 50);
    expect(pagamentoApi.listar).toHaveBeenCalledWith(50);
    expect(el().textContent).toContain('Banho');
    expect(el().textContent).toContain('Pendente');
  });

  it('exibe mensagem de erro quando o agendamento não é encontrado', () => {
    agendamentoApi.buscar.mockReturnValue(throwError(() => new Error('404')));
    criar();

    expect(el().querySelector('nz-alert')).toBeTruthy();
  });

  it('marca o pagamento como pago e recarrega a lista', () => {
    criar();
    pagamentoApi.listar.mockReturnValue(
      of([{ ...pagamentoPendente, status: 'PAGO', dataPagamento: '2026-01-01T10:00:00' }]),
    );

    fixture.componentInstance.marcarComoPago(pagamentoPendente);

    expect(pagamentoApi.atualizarStatus).toHaveBeenCalledWith(
      1,
      expect.objectContaining({ status: 'PAGO' }),
    );
    expect(pagamentoApi.listar).toHaveBeenCalledTimes(2);
  });

  it('cancela o pagamento após confirmação', () => {
    criar();

    fixture.componentInstance.confirmarCancelamento(pagamentoPendente);

    expect(confirmacao.perigo).toHaveBeenCalled();
    expect(pagamentoApi.cancelar).toHaveBeenCalledWith(1);
  });
});
