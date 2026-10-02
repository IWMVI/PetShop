import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import {
  Agendamento,
  AgendamentoRequest,
  ConsultaPaginada,
  Funcionario,
  FuncionarioRequest,
  AtualizarStatusPagamentoRequest,
  CategoriaLancamento,
  ContaFinanceiraRequest,
  DashboardGeral,
  ExtratoResponse,
  HistoricoPet,
  HistoricoPetRequest,
  LancamentoFinanceiro,
  LancamentoFinanceiroRequest,
  MarcarComoPagaRequest,
  Pagamento,
  PagamentoRequest,
  Pagina,
  Pet,
  PetRequest,
  SaldoContas,
  Servico,
  ServicoRequest,
  StatusLancamento,
  TipoLancamento,
  Tutor,
  TutorRequest,
  TutorResumo,
} from './models';

export const API_URL = '/api';

/** Quantidade de itens por página nas listagens. */
export const TAMANHO_PAGINA = 10;

function paramsDe(c: ConsultaPaginada = {}): HttpParams {
  let params = new HttpParams()
    .set('pagina', c.pagina ?? 0)
    .set('tamanho', c.tamanho ?? TAMANHO_PAGINA);
  if (c.busca?.trim()) params = params.set('busca', c.busca.trim());
  return params;
}

@Injectable({ providedIn: 'root' })
export class TutorApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/tutores`;

  listar(consulta?: ConsultaPaginada) {
    return this.http.get<Pagina<Tutor>>(this.url, { params: paramsDe(consulta) });
  }
  buscar(id: number) {
    return this.http.get<Tutor>(`${this.url}/${id}`);
  }
  criar(body: TutorRequest) {
    return this.http.post<Tutor>(this.url, body);
  }
  atualizar(id: number, body: TutorRequest) {
    return this.http.put<Tutor>(`${this.url}/${id}`, body);
  }
  excluir(id: number) {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
  restaurar(id: number) {
    return this.http.post<Tutor>(`${this.url}/${id}/restaurar`, null);
  }
  /** Localiza o tutor dono do CPF, inclusive excluído; 404 quando não há nenhum. */
  buscarPorCpf(cpf: string) {
    return this.http.get<TutorResumo>(`${this.url}/cpf/${cpf.replace(/\D/g, '')}`);
  }
}

@Injectable({ providedIn: 'root' })
export class PetApi {
  private readonly http = inject(HttpClient);
  private url(tutorId: number) {
    return `${API_URL}/tutores/${tutorId}/pets`;
  }

  listar(tutorId: number) {
    return this.http.get<Pet[]>(this.url(tutorId));
  }
  buscar(tutorId: number, petId: number) {
    return this.http.get<Pet>(`${this.url(tutorId)}/${petId}`);
  }
  criar(tutorId: number, body: PetRequest) {
    return this.http.post<Pet>(this.url(tutorId), body);
  }
  atualizar(tutorId: number, petId: number, body: PetRequest) {
    return this.http.put<Pet>(`${this.url(tutorId)}/${petId}`, body);
  }
  excluir(tutorId: number, petId: number) {
    return this.http.delete<void>(`${this.url(tutorId)}/${petId}`);
  }
}

@Injectable({ providedIn: 'root' })
export class ServicoApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/servicos`;

  listar(consulta?: ConsultaPaginada) {
    return this.http.get<Pagina<Servico>>(this.url, { params: paramsDe(consulta) });
  }
  buscar(id: number) {
    return this.http.get<Servico>(`${this.url}/${id}`);
  }
  criar(body: ServicoRequest) {
    return this.http.post<Servico>(this.url, body);
  }
  atualizar(id: number, body: ServicoRequest) {
    return this.http.put<Servico>(`${this.url}/${id}`, body);
  }
  excluir(id: number) {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}

@Injectable({ providedIn: 'root' })
export class AgendamentoApi {
  private readonly http = inject(HttpClient);
  private url(petId: number) {
    return `${API_URL}/pets/${petId}/agendamentos`;
  }

  listar(petId: number, consulta?: ConsultaPaginada) {
    return this.http.get<Pagina<Agendamento>>(this.url(petId), { params: paramsDe(consulta) });
  }
  buscar(petId: number, id: number) {
    return this.http.get<Agendamento>(`${this.url(petId)}/${id}`);
  }
  criar(petId: number, body: AgendamentoRequest) {
    return this.http.post<Agendamento>(this.url(petId), body);
  }
  atualizar(petId: number, id: number, body: AgendamentoRequest) {
    return this.http.put<Agendamento>(`${this.url(petId)}/${id}`, body);
  }
  cancelar(petId: number, id: number) {
    return this.http.delete<void>(`${this.url(petId)}/${id}`);
  }
}

@Injectable({ providedIn: 'root' })
export class FuncionarioApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/funcionarios`;

  listar(consulta?: ConsultaPaginada) {
    return this.http.get<Pagina<Funcionario>>(this.url, { params: paramsDe(consulta) });
  }
  buscar(id: number) {
    return this.http.get<Funcionario>(`${this.url}/${id}`);
  }
  criar(body: FuncionarioRequest) {
    return this.http.post<Funcionario>(this.url, body);
  }
  atualizar(id: number, body: FuncionarioRequest) {
    return this.http.put<Funcionario>(`${this.url}/${id}`, body);
  }
  excluir(id: number) {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}

/**
 * Pagamentos de um agendamento. Registrar e listar são aninhados sob o
 * agendamento; consultar, atualizar status e cancelar são por id do próprio
 * pagamento, espelhando o controller da API.
 */
@Injectable({ providedIn: 'root' })
export class PagamentoApi {
  private readonly http = inject(HttpClient);
  private url(agendamentoId: number) {
    return `${API_URL}/agendamentos/${agendamentoId}/pagamentos`;
  }
  private readonly urlPagamento = `${API_URL}/pagamentos`;

  listar(agendamentoId: number) {
    return this.http.get<Pagamento[]>(this.url(agendamentoId));
  }
  criar(agendamentoId: number, body: PagamentoRequest) {
    return this.http.post<Pagamento>(this.url(agendamentoId), body);
  }
  buscar(id: number) {
    return this.http.get<Pagamento>(`${this.urlPagamento}/${id}`);
  }
  atualizarStatus(id: number, body: AtualizarStatusPagamentoRequest) {
    return this.http.put<Pagamento>(`${this.urlPagamento}/${id}`, body);
  }
  cancelar(id: number) {
    return this.http.delete<void>(`${this.urlPagamento}/${id}`);
  }
}

/** Histórico do pet: só registra e consulta, pois os eventos são imutáveis na API. */
@Injectable({ providedIn: 'root' })
export class HistoricoPetApi {
  private readonly http = inject(HttpClient);
  private url(petId: number) {
    return `${API_URL}/pets/${petId}/historico`;
  }

  /** Eventos do pet, dos mais recentes para os mais antigos. */
  listar(petId: number) {
    return this.http.get<HistoricoPet[]>(this.url(petId));
  }
  buscar(petId: number, id: number) {
    return this.http.get<HistoricoPet>(`${this.url(petId)}/${id}`);
  }
  registrar(petId: number, body: HistoricoPetRequest) {
    return this.http.post<HistoricoPet>(this.url(petId), body);
  }
}

/** Filtros opcionais do extrato financeiro, além da paginação (que tem padrão). */
export interface ConsultaExtrato {
  inicio?: string;
  fim?: string;
  tipo?: TipoLancamento;
  categoria?: CategoriaLancamento;
  pagina?: number;
  tamanho?: number;
}

function paramsDeExtrato(c: ConsultaExtrato = {}): HttpParams {
  let params = new HttpParams()
    .set('pagina', c.pagina ?? 0)
    .set('tamanho', c.tamanho ?? TAMANHO_PAGINA);
  if (c.inicio) params = params.set('inicio', c.inicio);
  if (c.fim) params = params.set('fim', c.fim);
  if (c.tipo) params = params.set('tipo', c.tipo);
  if (c.categoria) params = params.set('categoria', c.categoria);
  return params;
}

/**
 * Lançamentos financeiros (caixa do pet shop): entradas e saídas manuais, contas a
 * pagar/receber e o extrato com os totais do período. Lançamentos com `pagamentoId`
 * preenchido foram gerados automaticamente por um pagamento e não podem ser editados
 * nem cancelados por aqui (a API recusa com 400).
 *
 * Contas a pagar/receber são a mesma entidade de lançamento, só criadas com vencimento
 * e sem data de pagamento (ficam `PENDENTE` até serem marcadas como pagas); por isso
 * `cancelar` é reaproveitado para cancelar uma conta pendente.
 */
@Injectable({ providedIn: 'root' })
export class FinanceiroApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/financeiro`;

  criar(body: LancamentoFinanceiroRequest) {
    return this.http.post<LancamentoFinanceiro>(`${this.url}/lancamentos`, body);
  }
  buscar(id: number) {
    return this.http.get<LancamentoFinanceiro>(`${this.url}/lancamentos/${id}`);
  }
  atualizar(id: number, body: LancamentoFinanceiroRequest) {
    return this.http.put<LancamentoFinanceiro>(`${this.url}/lancamentos/${id}`, body);
  }
  cancelar(id: number) {
    return this.http.delete<void>(`${this.url}/lancamentos/${id}`);
  }
  extrato(consulta?: ConsultaExtrato) {
    return this.http.get<ExtratoResponse>(`${this.url}/extrato`, {
      params: paramsDeExtrato(consulta),
    });
  }

  registrarConta(body: ContaFinanceiraRequest) {
    return this.http.post<LancamentoFinanceiro>(`${this.url}/contas`, body);
  }
  listarContas(
    tipo: TipoLancamento,
    status?: StatusLancamento,
    pagina = 0,
    tamanho = TAMANHO_PAGINA,
  ) {
    let params = new HttpParams().set('tipo', tipo).set('pagina', pagina).set('tamanho', tamanho);
    if (status) params = params.set('status', status);
    return this.http.get<Pagina<LancamentoFinanceiro>>(`${this.url}/contas`, { params });
  }
  saldoContas(tipo: TipoLancamento) {
    return this.http.get<SaldoContas>(`${this.url}/contas/saldo`, {
      params: new HttpParams().set('tipo', tipo),
    });
  }
  atualizarConta(id: number, body: ContaFinanceiraRequest) {
    return this.http.put<LancamentoFinanceiro>(`${this.url}/contas/${id}`, body);
  }
  marcarComoPaga(id: number, body: MarcarComoPagaRequest) {
    return this.http.put<LancamentoFinanceiro>(`${this.url}/contas/${id}/pagar`, body);
  }
}

/** Página inicial do sistema: resumo financeiro e agendamentos de hoje/próximos. */
@Injectable({ providedIn: 'root' })
export class DashboardApi {
  private readonly http = inject(HttpClient);

  dashboard() {
    return this.http.get<DashboardGeral>(`${API_URL}/dashboard`);
  }
}

/**
 * Extrai uma mensagem legível das respostas de erro da API.
 * Erros de domínio vêm como {@code { mensagem }}; erros de validação do
 * Spring vêm no formato padrão ({@code detail}, {@code errors} ou {@code message}).
 */
export function mensagemDeErro(err: unknown): string {
  if (!(err instanceof HttpErrorResponse)) {
    return 'Erro inesperado.';
  }
  if (err.status === 0) {
    return 'Não foi possível conectar à API. Verifique se o back-end está rodando.';
  }
  const body = err.error;
  if (body && typeof body === 'object') {
    if (body.mensagem) return body.mensagem;
    if (Array.isArray(body.errors) && body.errors.length) {
      return body.errors
        .map((e: { field?: string; defaultMessage?: string }) =>
          e.field ? `${e.field}: ${e.defaultMessage}` : e.defaultMessage,
        )
        .join('; ');
    }
    if (body.detail) return body.detail;
    if (body.message) return body.message;
  }
  if (err.status === 400) return 'Dados inválidos. Revise o formulário.';
  if (err.status === 404) return 'Registro não encontrado.';
  return `Erro ${err.status}: ${err.statusText || 'falha na requisição'}.`;
}
