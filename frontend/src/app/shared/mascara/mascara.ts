/**
 * Aplica uma máscara numérica: cada "0" do padrão é substituído por um dígito.
 * Aceita vários padrões separados por "|"; usa o menor que comporte os dígitos informados.
 *
 * Ex.: aplicarMascara('52998224725', '000.000.000-00') → '529.982.247-25'
 *      aplicarMascara('11987654321', '(00) 0000-0000|(00) 00000-0000') → '(11) 98765-4321'
 */
export function aplicarMascara(valor: string, padroes: string): string {
  const digitos = valor.replace(/\D/g, '');
  const padrao = escolherPadrao(digitos.length, padroes);
  let resultado = '';
  let i = 0;
  for (const c of padrao) {
    if (i >= digitos.length) break;
    resultado += c === '0' ? digitos[i++] : c;
  }
  return resultado;
}

function escolherPadrao(quantidade: number, padroes: string): string {
  const lista = padroes
    .split('|')
    .map((p) => ({ p, capacidade: [...p].filter((c) => c === '0').length }))
    .sort((a, b) => a.capacidade - b.capacidade);
  return (lista.find((x) => x.capacidade >= quantidade) ?? lista[lista.length - 1]).p;
}

export const MASCARAS = {
  cpf: '000.000.000-00',
  cep: '00000-000',
  telefone: '(00) 0000-0000|(00) 00000-0000',
} as const;
