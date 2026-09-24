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
