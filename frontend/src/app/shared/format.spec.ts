import {
  formatarMoeda,
  formatarPeso,
  formatarTelefone,
  paraLocalDateTime,
  vazioParaNull,
} from './format';

describe('format', () => {
  it('formata moeda em reais', () => {
    expect(formatarMoeda(49.9).replace(/\s/g, ' ')).toBe('R$ 49,90');
    expect(formatarMoeda(null)).toBe('—');
  });

  it('formata telefones fixos e celulares', () => {
    expect(formatarTelefone('11987654321')).toBe('(11) 98765-4321');
    expect(formatarTelefone('1133334444')).toBe('(11) 3333-4444');
    expect(formatarTelefone('123')).toBe('123');
  });

  it('gera LocalDateTime no fuso local, zerando os segundos', () => {
    expect(paraLocalDateTime(new Date(2099, 11, 31, 9, 5, 42))).toBe('2099-12-31T09:05:00');
  });

  it('converte vazio em null e preserva zero', () => {
    expect(vazioParaNull('')).toBeNull();
    expect(vazioParaNull(undefined)).toBeNull();
    expect(vazioParaNull(0)).toBe(0);
  });

  it('formata peso com vírgula decimal', () => {
    expect(formatarPeso(25.5)).toBe('25,5 kg');
    expect(formatarPeso(4)).toBe('4 kg');
  });
});
