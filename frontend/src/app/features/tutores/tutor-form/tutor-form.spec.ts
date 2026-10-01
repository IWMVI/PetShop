import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Subject, of, throwError } from 'rxjs';
import { provideTestEnv } from '../../../../testing/providers';
import { TutorApi } from '../../../core/api';
import { CepService, EnderecoCep } from '../../../core/cep/cep.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { TutorForm } from './tutor-form';

describe('TutorForm', () => {
  let fixture: ComponentFixture<TutorForm>;
  let api: jest.Mocked<
    Pick<TutorApi, 'buscar' | 'criar' | 'atualizar' | 'buscarPorCpf' | 'restaurar'>
  >;
  let respostaCep: Subject<EnderecoCep | null>;
  const cepService = { buscar: jest.fn() };

  beforeEach(async () => {
    api = {
      buscar: jest.fn(),
      criar: jest.fn().mockReturnValue(of({ id: 10 })),
      atualizar: jest.fn(),
      buscarPorCpf: jest
        .fn()
        .mockReturnValue(throwError(() => new HttpErrorResponse({ status: 404 }))),
      restaurar: jest.fn().mockReturnValue(of({ id: 7 })),
    };
    respostaCep = new Subject();
    cepService.buscar.mockReset().mockReturnValue(respostaCep);
    await TestBed.configureTestingModule({
      imports: [TutorForm],
      providers: [
        provideTestEnv(),
        { provide: TutorApi, useValue: api },
        { provide: CepService, useValue: cepService },
        { provide: ToastService, useValue: { sucesso: jest.fn(), erro: jest.fn() } },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(TutorForm);
    jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
  });

  const el = () => fixture.nativeElement as HTMLElement;
  const form = () => (fixture.componentInstance as unknown as { form: TutorForm['form'] }).form;
  const digitar = (id: string, texto: string) => {
    const input = el().querySelector<HTMLInputElement>(`#${id}`)!;
    input.value = texto;
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    return input;
  };

  it('não envia formulário inválido e destaca os campos', () => {
    el().querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
    expect(api.criar).not.toHaveBeenCalled();
    expect(el().querySelectorAll('.ant-form-item-has-error').length).toBeGreaterThanOrEqual(7);
  });

  it('aplica máscaras de CPF, telefone e CEP durante a digitação', () => {
    expect(digitar('cpf', '52998224725').value).toBe('529.982.247-25');
    expect(digitar('telefone', '1133334444').value).toBe('(11) 3333-4444');
    expect(digitar('telefone', '11987654321').value).toBe('(11) 98765-4321');
    expect(digitar('cep', '01001000').value).toBe('01001-000');
  });

  it('rejeita CPF com dígitos verificadores inválidos', () => {
    digitar('cpf', '12345678900');
    expect(form().controls.cpf.hasError('cpf')).toBe(true);
    digitar('cpf', '52998224725');
    expect(form().controls.cpf.valid).toBe(true);
  });

  it('valida o telefone com DDD', () => {
    digitar('telefone', '119999');
    expect(form().controls.telefone.hasError('pattern')).toBe(true);
    digitar('telefone', '1133334444');
    expect(form().controls.telefone.valid).toBe(true);
  });

  it('preenche o endereço a partir do CEP', () => {
    digitar('cep', '01001000');
    expect(cepService.buscar).toHaveBeenCalledWith('01001000');
    expect(el().textContent).toContain('Buscando endereço…');

    respostaCep.next({
      cep: '01001-000',
      logradouro: 'Praça da Sé',
      complemento: 'lado ímpar',
      bairro: 'Sé',
      cidade: 'São Paulo',
      estado: 'SP',
    });
    fixture.detectChanges();

    expect(form().controls.endereco.getRawValue()).toEqual({
      cep: '01001-000',
      logradouro: 'Praça da Sé',
      numero: '',
      complemento: 'lado ímpar',
      bairro: 'Sé',
      cidade: 'São Paulo',
      estado: 'SP',
    });
    expect(document.activeElement?.id).toBe('numero');
    expect(el().textContent).toContain('Endereço preenchido pelo CEP.');
  });

  it('avisa quando o CEP não é encontrado', () => {
    digitar('cep', '99999999');
    respostaCep.next(null);
    fixture.detectChanges();
    expect(el().textContent).toContain('CEP não encontrado');
  });

  it('não busca o CEP ao carregar um tutor para edição', () => {
    api.buscar.mockReturnValue(
      of({
        id: 5,
        nome: 'Ana',
        cpf: '52998224725',
        email: 'ana@test.com',
        telefone: '11987654321',
        endereco: {
          cep: '01001000',
          logradouro: 'Rua Salva',
          bairro: 'Centro',
          cidade: 'São Paulo',
          estado: 'SP',
        },
      }),
    );
    fixture = TestBed.createComponent(TutorForm);
    fixture.componentRef.setInput('id', 5);
    fixture.detectChanges();

    expect(cepService.buscar).not.toHaveBeenCalled();
    expect(form().getRawValue()).toMatchObject({
      cpf: '529.982.247-25',
      telefone: '(11) 98765-4321',
      endereco: { cep: '01001-000', logradouro: 'Rua Salva' },
    });
  });

  it('envia CPF e telefone só com dígitos, normalizando opcionais e UF', () => {
    form().setValue({
      nome: 'Ana',
      cpf: '529.982.247-25',
      email: 'ana@test.com',
      telefone: '(11) 98765-4321',
      endereco: {
        cep: '01001-000',
        logradouro: 'Praça da Sé',
        numero: '',
        complemento: '',
        bairro: 'Sé',
        cidade: 'São Paulo',
        estado: 'sp',
      },
    });
    el().querySelector('form')!.dispatchEvent(new Event('submit'));

    expect(api.criar).toHaveBeenCalledWith({
      nome: 'Ana',
      cpf: '52998224725',
      email: 'ana@test.com',
      telefone: '11987654321',
      endereco: {
        cep: '01001-000',
        logradouro: 'Praça da Sé',
        numero: null,
        complemento: null,
        bairro: 'Sé',
        cidade: 'São Paulo',
        estado: 'SP',
      },
    });
    expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith(['/tutores', 10]);
  });

  describe('CPF já cadastrado', () => {
    const excluido = { id: 7, nome: 'Carla Antiga', email: 'carla@test.com', excluido: true };
    const modal = () => document.querySelector<HTMLElement>('.ant-modal');
    const botaoModal = (texto: string) =>
      Array.from(document.querySelectorAll<HTMLButtonElement>('.ant-modal-footer button')).find(
        (b) => b.textContent?.trim() === texto,
      )!;

    it('não abre o modal quando o CPF está livre', () => {
      digitar('cpf', '52998224725');
      expect(api.buscarPorCpf).toHaveBeenCalledWith('52998224725');
      expect(modal()).toBeNull();
    });

    it('não consulta a API enquanto o CPF for inválido', () => {
      digitar('cpf', '12345678900');
      expect(api.buscarPorCpf).not.toHaveBeenCalled();
    });

    it('oferece restaurar o cadastro de um tutor excluído', () => {
      api.buscarPorCpf.mockReturnValue(of(excluido));
      digitar('cpf', '52998224725');

      expect(modal()?.textContent).toContain('Recuperar cadastro');
      expect(modal()?.textContent).toContain('Carla Antiga');
      expect(modal()?.textContent).toContain('carla@test.com');

      botaoModal('Restaurar cadastro').click();
      fixture.detectChanges();

      expect(api.restaurar).toHaveBeenCalledWith(7);
      expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith(['/tutores', 7, 'editar']);
    });

    it('aponta o cadastro ativo quando o CPF pertence a outro tutor', () => {
      api.buscarPorCpf.mockReturnValue(of({ ...excluido, excluido: false }));
      digitar('cpf', '52998224725');

      expect(modal()?.textContent).toContain('CPF já cadastrado');
      botaoModal('Ver cadastro').click();
      expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith(['/tutores', 7]);
    });

    it('limpa o CPF ao escolher informar outro', () => {
      api.buscarPorCpf.mockReturnValue(of(excluido));
      digitar('cpf', '52998224725');
      botaoModal('Informar outro CPF').click();
      fixture.detectChanges();

      expect(form().controls.cpf.value).toBe('');
      expect(api.restaurar).not.toHaveBeenCalled();
    });

    it('abre o modal se a API recusar o CPF ao salvar (409)', () => {
      api.criar.mockReturnValue(
        throwError(
          () => new HttpErrorResponse({ status: 409, error: { mensagem: 'CPF já cadastrado.' } }),
        ),
      );
      form().setValue({
        nome: 'Ana',
        cpf: '529.982.247-25',
        email: 'ana@test.com',
        telefone: '(11) 98765-4321',
        endereco: {
          cep: '01001-000',
          logradouro: 'Rua A',
          numero: '',
          complemento: '',
          bairro: 'Sé',
          cidade: 'São Paulo',
          estado: 'SP',
        },
      });
      api.buscarPorCpf.mockReturnValue(of(excluido));
      el().querySelector('form')!.dispatchEvent(new Event('submit'));
      fixture.detectChanges();

      expect(modal()?.textContent).toContain('Recuperar cadastro');
    });
  });
});
