import { MASCARAS, aplicarMascara } from './mascara';

describe('aplicarMascara', () => {
  it('formata CPF', () => {
    expect(aplicarMascara('52998224725', MASCARAS.cpf)).toBe('529.982.247-25');
  });

  it('formata parcialmente durante a digitação', () => {
    expect(aplicarMascara('5299', MASCARAS.cpf)).toBe('529.9');
  });

  it('ignora caracteres não numéricos e o excesso de dígitos', () => {
    expect(aplicarMascara('01001-000123', MASCARAS.cep)).toBe('01001-000');
  });

  it('usa a máscara de telefone fixo com 10 dígitos', () => {
    expect(aplicarMascara('1133334444', MASCARAS.telefone)).toBe('(11) 3333-4444');
  });

  it('muda para a máscara de celular com 11 dígitos', () => {
    expect(aplicarMascara('11987654321', MASCARAS.telefone)).toBe('(11) 98765-4321');
  });
});
