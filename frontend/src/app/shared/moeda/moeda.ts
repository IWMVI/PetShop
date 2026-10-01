const formato = new Intl.NumberFormat('pt-BR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

/** Formata um número no padrão brasileiro com 2 casas: 1234.5 → "1.234,50". */
export function formatarValor(valor: number): string {
  return formato.format(valor);
}

/**
 * Interpreta o texto digitado como centavos, preenchendo da direita para a esquerda:
 * "1" → 0,01; "10" → 0,10; "100" → 1,00; "100000" → 1.000,00.
 * Retorna null quando não há dígitos.
 */
export function centavosDigitados(texto: string, maximo: number): number | null {
  const digitos = texto.replace(/\D/g, '').replace(/^0+/, '');
  if (!digitos) return texto.replace(/\D/g, '') ? 0 : null;
  const valor = Number(digitos) / 100;
  return Math.min(valor, maximo);
}
