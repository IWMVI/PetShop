import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { provideTestEnv } from '../../../../testing/providers';
import { PagamentoApi } from '../../../core/api';
import { ToastService } from '../../../shared/toast/toast.service';
import { PagamentoForm } from './pagamento-form';

describe('PagamentoForm', () => {
  let fixture: ComponentFixture<PagamentoForm>;
  let api: jest.Mocked<Pick<PagamentoApi, 'criar'>>;

  beforeEach(async () => {
    api = { criar: jest.fn().mockReturnValue(of({})) };

    await TestBed.configureTestingModule({
      imports: [PagamentoForm],
      providers: [
        provideTestEnv(),
        { provide: PagamentoApi, useValue: api },
        { provide: ToastService, useValue: { sucesso: jest.fn(), erro: jest.fn() } },
      ],
    }).compileComponents();
    jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  });

  const criar = () => {
    fixture = TestBed.createComponent(PagamentoForm);
    fixture.componentRef.setInput('tutorId', 1);
    fixture.componentRef.setInput('petId', 7);
    fixture.componentRef.setInput('agendamentoId', 50);
    fixture.detectChanges();
  };

  const el = () => fixture.nativeElement as HTMLElement;
  const submeter = () => {
    el().querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  };

  it('exige valor e método de pagamento', () => {
    criar();
    submeter();

    expect(api.criar).not.toHaveBeenCalled();
    expect(el().textContent).toContain('Informe um valor maior que zero.');
    expect(el().textContent).toContain('Selecione o método de pagamento.');
  });

  it('registra o pagamento e volta para o agendamento', () => {
    criar();
    fixture.componentInstance.form.setValue({ valor: 150, metodoPagamento: 'PIX' });
    submeter();

    expect(api.criar).toHaveBeenCalledWith(50, { valor: 150, metodoPagamento: 'PIX' });
    expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith([
      '/tutores',
      1,
      'pets',
      7,
      'agendamentos',
      50,
    ]);
  });
});
