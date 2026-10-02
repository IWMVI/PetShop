import {
  Cargo,
  CategoriaLancamento,
  MetodoPagamento,
  StatusPagamento,
  TipoEvento,
  TipoLancamento,
} from '../../core/models';
import { TomStatus } from '../status/status';

/** Rótulos dos cargos, na ordem em que aparecem nos formulários. */
export const CARGOS: Record<Cargo, string> = {
  VETERINARIO: 'Veterinário(a)',
  TOSADOR: 'Tosador(a)',
  BANHISTA: 'Banhista',
  ATENDENTE: 'Atendente',
  GERENTE: 'Gerente',
};

/** Rótulo e tom de cada tipo de evento do histórico, exibidos com o <app-status>. */
export const TIPOS_EVENTO: Record<TipoEvento, { rotulo: string; tom: TomStatus }> = {
  VACINACAO: { rotulo: 'Vacinação', tom: 'sucesso' },
  CONSULTA: { rotulo: 'Consulta', tom: 'destaque' },
  PROCEDIMENTO: { rotulo: 'Procedimento', tom: 'alerta' },
  SERVICO: { rotulo: 'Serviço', tom: 'neutro' },
  OUTRO: { rotulo: 'Outro', tom: 'neutro' },
};

/** Rótulos dos métodos de pagamento, na ordem em que aparecem nos formulários. */
export const METODOS_PAGAMENTO: Record<MetodoPagamento, string> = {
  CARTAO_CREDITO: 'Cartão de crédito',
  CARTAO_DEBITO: 'Cartão de débito',
  PIX: 'Pix',
  DINHEIRO: 'Dinheiro',
};

/** Rótulo e tom de cada status de pagamento, exibidos com o <app-status>. */
export const STATUS_PAGAMENTO: Record<StatusPagamento, { rotulo: string; tom: TomStatus }> = {
  PENDENTE: { rotulo: 'Pendente', tom: 'destaque' },
  PAGO: { rotulo: 'Pago', tom: 'sucesso' },
  CANCELADO: { rotulo: 'Cancelado', tom: 'neutro' },
};

/** Rótulo e tom de cada tipo de lançamento financeiro, exibidos com o <app-status>. */
export const TIPOS_LANCAMENTO: Record<TipoLancamento, { rotulo: string; tom: TomStatus }> = {
  ENTRADA: { rotulo: 'Entrada', tom: 'sucesso' },
  SAIDA: { rotulo: 'Saída', tom: 'erro' },
};

/** Rótulos das categorias de lançamento financeiro. */
export const CATEGORIAS_LANCAMENTO: Record<CategoriaLancamento, string> = {
  PAGAMENTO_SERVICO: 'Pagamento de serviço',
  VENDA_PRODUTO: 'Venda de produto',
  OUTRA_RECEITA: 'Outra receita',
  ALUGUEL: 'Aluguel',
  SALARIO: 'Salário',
  FORNECEDOR: 'Fornecedor',
  MANUTENCAO: 'Manutenção',
  IMPOSTO: 'Imposto',
  OUTRA_DESPESA: 'Outra despesa',
};

/** Categorias válidas para lançamentos do tipo ENTRADA, na ordem do formulário. */
export const CATEGORIAS_ENTRADA: CategoriaLancamento[] = [
  'PAGAMENTO_SERVICO',
  'VENDA_PRODUTO',
  'OUTRA_RECEITA',
];

/** Categorias válidas para lançamentos do tipo SAIDA, na ordem do formulário. */
export const CATEGORIAS_SAIDA: CategoriaLancamento[] = [
  'ALUGUEL',
  'SALARIO',
  'FORNECEDOR',
  'MANUTENCAO',
  'IMPOSTO',
  'OUTRA_DESPESA',
];

/** Lista de opções {valor, rótulo} para selects, preservando a ordem de declaração. */
export function opcoes<T extends string>(mapa: Record<T, string | { rotulo: string }>) {
  return (Object.entries(mapa) as [T, string | { rotulo: string }][]).map(([valor, r]) => ({
    valor,
    rotulo: typeof r === 'string' ? r : r.rotulo,
  }));
}
