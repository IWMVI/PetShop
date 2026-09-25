import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { provideTestEnv } from '../../../../testing/providers';
import { FuncionarioApi } from '../../../core/api';
import { ToastService } from '../../../shared/toast/toast.service';
import { FuncionarioForm } from './funcionario-form';

describe('FuncionarioForm', () => {
  let fixture: ComponentFixture<FuncionarioForm>;
  let api: jest.Mocked<Pick<FuncionarioApi, 'buscar' | 'criar' | 'atualizar'>>;
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
      imports: [FuncionarioForm],
      providers: [
        provideTestEnv(),
        { provide: FuncionarioApi, useValue: api },
        { provide: ToastService, useValue: toast },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(FuncionarioForm);
    jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
  });

  const el = () => fixture.nativeElement as HTMLElement;
  const form = () =>
    (fixture.componentInstance as unknown as { form: FuncionarioForm['form'] }).form;
  const digitar = (id: string, texto: string) => {
    const input = el().querySelector<HTMLInputElement>(`#${id}`)!;
    input.value = texto;
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    return input;
  };
  const enviar = () => {
    el().querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  };
  const preencher = () =>
    form().setValue({
      nome: 'Dra. Ana',
      cpf: '529.982.247-25',
      cargo: 'VETERINARIO',
      telefone: '(11) 98888-7777',
    });

  it('não envia formulário inválido e destaca os campos', () => {
    enviar();
    expect(api.criar).not.toHaveBeenCalled();
    expect(el().querySelectorAll('.ant-form-item-has-error').length).toBe(4);
  });

  it('aplica máscaras de CPF e telefone durante a digitação', () => {
    expect(digitar('cpf', '52998224725').value).toBe('529.982.247-25');
    expect(digitar('telefone', '11988887777').value).toBe('(11) 98888-7777');
  });

  it('rejeita CPF com dígitos verificadores inválidos', () => {
    digitar('cpf', '12345678900');
    expect(form().controls.cpf.hasError('cpf')).toBe(true);
    digitar('cpf', '52998224725');
    expect(form().controls.cpf.valid).toBe(true);
  });

  it('exige o cargo', () => {
    expect(form().controls.cargo.hasError('required')).toBe(true);
  });

  it('exige nome com ao menos 3 caracteres', () => {
    digitar('nome', 'Al');
    expect(form().controls.nome.hasError('minlength')).toBe(true);
    digitar('nome', 'Ana');
    expect(form().controls.nome.valid).toBe(true);
  });

  it('envia CPF e telefone só com dígitos e volta para a lista', () => {
    preencher();
    enviar();

    expect(api.criar).toHaveBeenCalledWith({
      nome: 'Dra. Ana',
      cpf: '52998224725',
      cargo: 'VETERINARIO',
      telefone: '11988887777',
    });
    expect(toast.sucesso).toHaveBeenCalledWith('Funcionário cadastrado.');
    expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith(['/funcionarios']);
  });

  it('mostra o erro da API quando o CPF já está cadastrado', () => {
    api.criar.mockReturnValue(
      throwError(
        () => new HttpErrorResponse({ status: 409, error: { mensagem: 'CPF já cadastrado.' } }),
      ),
    );
    preencher();
    enviar();

    expect(toast.erro).toHaveBeenCalledWith('CPF já cadastrado.');
    expect(TestBed.inject(Router).navigate).not.toHaveBeenCalled();
  });

  it('carrega o funcionário para edição com máscaras e atualiza', () => {
    api.buscar.mockReturnValue(
      of({ id: 5, nome: 'Bia', cpf: '52998224725', cargo: 'TOSADOR', telefone: '1133334444' }),
    );
    fixture = TestBed.createComponent(FuncionarioForm);
    jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.componentRef.setInput('id', 5);
    fixture.detectChanges();

    expect(form().getRawValue()).toEqual({
      nome: 'Bia',
      cpf: '529.982.247-25',
      cargo: 'TOSADOR',
      telefone: '(11) 3333-4444',
    });

    enviar();
    expect(api.atualizar).toHaveBeenCalledWith(5, {
      nome: 'Bia',
      cpf: '52998224725',
      cargo: 'TOSADOR',
      telefone: '1133334444',
    });
    expect(toast.sucesso).toHaveBeenCalledWith('Funcionário atualizado.');
  });
});
