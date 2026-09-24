import { FormControl } from '@angular/forms';
import { cpfValidator, cpfValido } from './cpf.validator';

describe('cpfValido', () => {
  it.each(['529.982.247-25', '52998224725', '111.444.777-35'])('aceita %s', (cpf) => {
    expect(cpfValido(cpf)).toBe(true);
  });

  it.each([
    ['dígito verificador errado', '529.982.247-24'],
    ['todos os dígitos iguais', '111.111.111-11'],
    ['menos de 11 dígitos', '5299822472'],
    ['mais de 11 dígitos', '529982247250'],
    ['vazio', ''],
  ])('rejeita %s', (_, cpf) => {
    expect(cpfValido(cpf)).toBe(false);
  });
});

describe('cpfValidator', () => {
  it('ignora valor vazio (responsabilidade do required)', () => {
    expect(cpfValidator(new FormControl(''))).toBeNull();
  });

  it('retorna erro "cpf" para CPF inválido', () => {
    expect(cpfValidator(new FormControl('123.456.789-00'))).toEqual({ cpf: true });
  });
});
