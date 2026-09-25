import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { provideTestEnv } from '../../../../testing/providers';
import { FuncionarioApi, HistoricoPetApi } from '../../../core/api';
import { ToastService } from '../../../shared/toast/toast.service';
import { HistoricoForm } from './historico-form';

describe('HistoricoForm', () => {
  let fixture: ComponentFixture<HistoricoForm>;
  let api: jest.Mocked<Pick<HistoricoPetApi, 'registrar'>>;
  let funcionarioApi: jest.Mocked<Pick<FuncionarioApi, 'listar'>>;
  const toast = { sucesso: jest.fn(), erro: jest.fn() };

  beforeEach(async () => {
    toast.sucesso.mockReset();
    toast.erro.mockReset();
    api = { registrar: jest.fn().mockReturnValue(of({ id: 1 })) };
    funcionarioApi = {
      listar: jest.fn().mockReturnValue(
        of({
          itens: [
            { id: 3, nome: 'Dra. Ana', cpf: '52998224725', cargo: 'VETERINARIO', telefone: '1' },
          ],
          pagina: 0,
          tamanho: 10,
          total: 1,
          totalPaginas: 1,
        }),
      ),
    };
    await TestBed.configureTestingModule({
      imports: [HistoricoForm],
      providers: [
        provideTestEnv(),
        { provide: HistoricoPetApi, useValue: api },
        { provide: FuncionarioApi, useValue: funcionarioApi },
        { provide: ToastService, useValue: toast },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(HistoricoForm);
    fixture.componentRef.setInput('tutorId', 1);
    fixture.componentRef.setInput('petId', 7);
    jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
  });

  const el = () => fixture.nativeElement as HTMLElement;
  const form = () => (fixture.componentInstance as unknown as { form: HistoricoForm['form'] }).form;
  const enviar = () => {
    el().querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  };
  const ontem = () => new Date(Date.now() - 24 * 60 * 60 * 1000);

  it('carrega a primeira página de funcionários para o select', () => {
    expect(funcionarioApi.listar).toHaveBeenCalledWith({ busca: '' });
  });

  it('não envia formulário inválido e destaca os campos obrigatórios', () => {
    enviar();
    expect(api.registrar).not.toHaveBeenCalled();
    // tipo, data e descrição são obrigatórios; o funcionário é opcional.
    expect(el().querySelectorAll('.ant-form-item-has-error').length).toBe(3);
  });

  it('rejeita descrição só com espaços', () => {
    form().controls.descricao.setValue('   ');
    expect(form().controls.descricao.hasError('pattern')).toBe(true);
  });

  it('rejeita data do evento no futuro', () => {
    form().controls.dataEvento.setValue(new Date(Date.now() + 60 * 60 * 1000));
    expect(form().controls.dataEvento.hasError('futura')).toBe(true);
    form().controls.dataEvento.setValue(ontem());
    expect(form().controls.dataEvento.valid).toBe(true);
  });

  it('registra o evento com funcionário e volta para o pet', () => {
    const data = new Date(2026, 0, 10, 14, 30);
    form().setValue({
      tipoEvento: 'VACINACAO',
      dataEvento: data,
      funcionarioId: 3,
      descricao: '  Vacina V10  ',
    });
    enviar();

    expect(api.registrar).toHaveBeenCalledWith(7, {
      tipoEvento: 'VACINACAO',
      dataEvento: '2026-01-10T14:30:00',
      funcionarioId: 3,
      descricao: 'Vacina V10',
    });
    expect(toast.sucesso).toHaveBeenCalledWith('Evento registrado no histórico.');
    expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith(['/tutores', 1, 'pets', 7]);
  });

  it('registra evento externo sem funcionário', () => {
    form().setValue({
      tipoEvento: 'OUTRO',
      dataEvento: ontem(),
      funcionarioId: null,
      descricao: 'Vacina aplicada em outra clínica',
    });
    enviar();

    expect(api.registrar).toHaveBeenCalledWith(
      7,
      expect.objectContaining({ tipoEvento: 'OUTRO', funcionarioId: null }),
    );
  });

  it('mostra o erro da API e permanece no formulário', () => {
    api.registrar.mockReturnValue(
      throwError(
        () => new HttpErrorResponse({ status: 404, error: { mensagem: 'Pet não encontrado.' } }),
      ),
    );
    form().setValue({
      tipoEvento: 'CONSULTA',
      dataEvento: ontem(),
      funcionarioId: null,
      descricao: 'Retorno',
    });
    enviar();

    expect(toast.erro).toHaveBeenCalledWith('Pet não encontrado.');
    expect(TestBed.inject(Router).navigate).not.toHaveBeenCalled();
  });
});
