/** Página de uma listagem paginada da API. */
export interface Pagina<T> {
  itens: T[];
  /** Índice da página, começando em 0. */
  pagina: number;
  tamanho: number;
  total: number;
  totalPaginas: number;
}

/** Parâmetros de listagem paginada. */
export interface ConsultaPaginada {
  pagina?: number;
  tamanho?: number;
  busca?: string;
}

export interface Endereco {
  cep: string;
  logradouro: string;
  numero?: string | null;
  complemento?: string | null;
  bairro: string;
  cidade: string;
  estado?: string | null;
}

export interface Tutor {
  id: number;
  nome: string;
  /** Apenas dígitos na resposta; a API também aceita com máscara. */
  cpf: string | null;
  email: string;
  telefone: string;
  endereco: Endereco;
}

/** Dono de um CPF, inclusive tutor excluído (para oferecer a recuperação do cadastro). */
export interface TutorResumo {
  id: number;
  nome: string;
  email: string;
  excluido: boolean;
}

export type TutorRequest = Omit<Tutor, 'id'>;

export interface Pet {
  id: number;
  nome: string;
  especie: string;
  raca?: string | null;
  idade?: number | null;
  peso?: number | null;
}

export type PetRequest = Omit<Pet, 'id'>;

export interface Servico {
  id: number;
  nome: string;
  descricao?: string | null;
  preco: number;
  tempoEstimadoMinutos?: number | null;
}

export type ServicoRequest = Omit<Servico, 'id'>;

export type AgendamentoStatus = 'AGENDADO' | 'CANCELADO' | 'CONCLUIDO';

export interface AgendamentoServico {
  servicoId: number;
  nome: string | null;
  precoCobrado: number;
}

export interface Agendamento {
  id: number;
  petId: number;
  dataHora: string;
  observacoes?: string | null;
  status: AgendamentoStatus;
  valorTotal: number;
  servicos: AgendamentoServico[];
}

export interface AgendamentoRequest {
  dataHora: string;
  observacoes?: string | null;
  servicoIds: number[];
}

export type StatusPagamento = 'PENDENTE' | 'PAGO' | 'CANCELADO';
export type MetodoPagamento = 'CARTAO_CREDITO' | 'CARTAO_DEBITO' | 'PIX' | 'DINHEIRO';

export interface Pagamento {
  id: number;
  agendamentoId: number;
  valor: number;
  metodoPagamento: MetodoPagamento;
  status: StatusPagamento;
  dataPagamento: string | null;
}

export interface PagamentoRequest {
  valor: number;
  metodoPagamento: MetodoPagamento;
}

export interface AtualizarStatusPagamentoRequest {
  status: StatusPagamento;
  dataPagamento?: string | null;
}

export type Cargo = 'VETERINARIO' | 'TOSADOR' | 'BANHISTA' | 'ATENDENTE' | 'GERENTE';

export interface Funcionario {
  id: number;
  nome: string;
  /** Apenas dígitos na resposta; a API também aceita com máscara. */
  cpf: string;
  cargo: Cargo;
  telefone: string;
}

export type FuncionarioRequest = Omit<Funcionario, 'id'>;

export type TipoEvento = 'VACINACAO' | 'CONSULTA' | 'PROCEDIMENTO' | 'SERVICO' | 'OUTRO';

/** Evento do histórico do pet. Registro imutável: a API não permite alterar nem excluir. */
export interface HistoricoPet {
  id: number;
  petId: number;
  tipoEvento: TipoEvento;
  descricao: string;
  dataEvento: string;
  /** Nulo em eventos externos (ex.: vacina aplicada em outra clínica). */
  funcionarioId: number | null;
  funcionarioNome: string | null;
  createdAt: string;
}

export interface HistoricoPetRequest {
  tipoEvento: TipoEvento;
  descricao: string;
  dataEvento: string;
  funcionarioId: number | null;
}

export type TipoLancamento = 'ENTRADA' | 'SAIDA';

export type CategoriaLancamento =
  // Categorias de ENTRADA
  | 'PAGAMENTO_SERVICO'
  | 'VENDA_PRODUTO'
  | 'OUTRA_RECEITA'
  // Categorias de SAIDA
  | 'ALUGUEL'
  | 'SALARIO'
  | 'FORNECEDOR'
  | 'MANUTENCAO'
  | 'IMPOSTO'
  | 'OUTRA_DESPESA';

export type StatusLancamento = 'PENDENTE' | 'PAGO' | 'CANCELADO';

export interface LancamentoFinanceiro {
  id: number;
  tipo: TipoLancamento;
  categoria: CategoriaLancamento;
  descricao: string;
  valor: number;
  status: StatusLancamento;
  /** Vencimento de uma conta a pagar/receber; nulo em lançamentos já realizados. */
  dataVencimento: string | null;
  /** Preenchida quando o lançamento foi realizado; nula enquanto a conta está pendente. */
  dataPagamento: string | null;
  /** Computado pelo back-end: true quando ainda PENDENTE e a data de vencimento já passou. */
  vencido: boolean;
  /** Preenchido quando o lançamento foi gerado automaticamente por um pagamento; nesse caso é imutável por aqui. */
  pagamentoId: number | null;
}

/** Lançamento avulso já realizado (entrada/saída de caixa), criado a partir da tela Extrato. */
export interface LancamentoFinanceiroRequest {
  tipo: TipoLancamento;
  categoria: CategoriaLancamento;
  descricao: string;
  valor: number;
  dataPagamento: string;
}

/** Conta a pagar/receber: ainda não realizada, tem vencimento mas nenhuma data de pagamento. */
export interface ContaFinanceiraRequest {
  tipo: TipoLancamento;
  categoria: CategoriaLancamento;
  descricao: string;
  valor: number;
  dataVencimento: string;
}

export interface MarcarComoPagaRequest {
  dataPagamento: string;
}

/** Totais de contas pendentes de um tipo (a pagar ou a receber). */
export interface SaldoContas {
  totalPendente: number;
  totalVencido: number;
}

/** Extrato financeiro de um período: lançamentos paginados e os totais do período. */
export interface ExtratoResponse {
  lancamentos: Pagina<LancamentoFinanceiro>;
  totalEntradas: number;
  totalSaidas: number;
  saldo: number;
}
