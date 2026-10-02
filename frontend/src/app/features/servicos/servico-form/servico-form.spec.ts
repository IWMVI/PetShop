import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { of, throwError } from 'rxjs';
import { provideTestEnv } from '../../../../testing/providers';
import { ServicoApi } from '../../../core/api';
import { ToastService } from '../../../shared/toast/toast.service';
import { ServicoForm } from './servico-form';

describe('ServicoForm', () => {
  let fixture: ComponentFixture<ServicoForm>;
  let api: jest.Mocked<Pick<ServicoApi, 'buscar' | 'criar' | 'atualizar'>>;
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
      imports: [ServicoForm],
      providers: [
        provideTestEnv(),
        { provide: ServicoApi, useValue: api },
        { provide: ToastService, useValue: toast },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(ServicoForm);
    jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
  });

  const el = () => fixture.nativeElement as HTMLElement;
  const form = () => (fixture.componentInstance as unknown as { form: ServicoForm['form'] }).form;
  const enviar = () => {
    el().querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  };
  const preencher = () =>
    form().setValue({
      nome: 'Banho e Tosa',
      descricao: 'Banho completo',
      preco: 150,
      tempoEstimadoMinutos: 60,
    });

  it('não envia formulário inválido e destaca os campos', () => {
    enviar();
    expect(api.criar).not.toHaveBeenCalled();
    expect(el().querySelectorAll('.ant-form-item-has-error').length).toBeGreaterThan(0);
  });

  it('cadastra o serviço e volta para a lista', () => {
    preencher();
    enviar();

    expect(api.criar).toHaveBeenCalledWith({
      nome: 'Banho e Tosa',
      descricao: 'Banho completo',
      preco: 150,
      tempoEstimadoMinutos: 60,
    });
    expect(toast.sucesso).toHaveBeenCalledWith('Serviço cadastrado.');
    expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith(['/servicos']);
  });

  it('mostra o erro da API e permanece no formulário', () => {
    api.criar.mockReturnValue(
      throwError(
        () => new HttpErrorResponse({ status: 400, error: { mensagem: 'Preço inválido.' } }),
      ),
    );
    preencher();
    enviar();

    expect(toast.erro).toHaveBeenCalledWith('Preço inválido.');
    expect(TestBed.inject(Router).navigate).not.toHaveBeenCalled();
  });

  it('carrega o serviço existente para edição e atualiza', () => {
    api.buscar.mockReturnValue(
      of({ id: 5, nome: 'Tosa', descricao: null, preco: 80, tempoEstimadoMinutos: 40 }),
    );
    fixture = TestBed.createComponent(ServicoForm);
    jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.componentRef.setInput('id', 5);
    fixture.detectChanges();

    expect(form().value.nome).toBe('Tosa');

    enviar();
    expect(api.atualizar).toHaveBeenCalledWith(5, expect.objectContaining({ nome: 'Tosa' }));
    expect(toast.sucesso).toHaveBeenCalledWith('Serviço atualizado.');
  });

  describe('como modal de cadastro rápido', () => {
    let modalRef: jest.Mocked<Pick<NzModalRef, 'close'>>;

    beforeEach(async () => {
      modalRef = { close: jest.fn() };
      TestBed.resetTestingModule();
      await TestBed.configureTestingModule({
        imports: [ServicoForm],
        providers: [
          provideTestEnv(),
          { provide: ServicoApi, useValue: api },
          { provide: ToastService, useValue: toast },
          { provide: NzModalRef, useValue: modalRef },
        ],
      }).compileComponents();
      fixture = TestBed.createComponent(ServicoForm);
      jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      fixture.detectChanges();
    });

    it('não mostra a trilha/breadcrumb da página', () => {
      expect(el().querySelector('nz-breadcrumb')).toBeNull();
    });

    it('fecha o modal com o serviço criado em vez de navegar', () => {
      preencher();
      enviar();

      expect(api.criar).toHaveBeenCalled();
      expect(modalRef.close).toHaveBeenCalledWith({ id: 1 });
      expect(TestBed.inject(Router).navigate).not.toHaveBeenCalled();
    });
  });
});
