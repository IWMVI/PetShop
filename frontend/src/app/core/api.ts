import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import {
  Agendamento,
  AgendamentoRequest,
  ConsultaPaginada,
  Pagina,
  Pet,
  PetRequest,
  Servico,
  ServicoRequest,
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
