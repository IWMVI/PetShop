import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AgendamentoApi, PetApi, ServicoApi, TutorApi, mensagemDeErro } from './api';

describe('APIs', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('TutorApi lista de forma paginada, com 10 itens por padrão', () => {
    const api = TestBed.inject(TutorApi);
    api.listar().subscribe();
    const req = http.expectOne((r) => r.method === 'GET' && r.url === '/api/tutores');
    expect(req.request.params.get('pagina')).toBe('0');
    expect(req.request.params.get('tamanho')).toBe('10');
    expect(req.request.params.has('busca')).toBe(false);
    req.flush({ itens: [], pagina: 0, tamanho: 10, total: 0, totalPaginas: 0 });
  });

  it('TutorApi envia a busca e a página pedida', () => {
    TestBed.inject(TutorApi).listar({ pagina: 2, busca: '  ana ' }).subscribe();
    const req = http.expectOne((r) => r.url === '/api/tutores');
    expect(req.request.params.get('pagina')).toBe('2');
    expect(req.request.params.get('busca')).toBe('ana');
    req.flush({ itens: [], pagina: 2, tamanho: 10, total: 0, totalPaginas: 0 });
  });

  it('TutorApi busca pelo CPF só com dígitos e restaura cadastro', () => {
    const api = TestBed.inject(TutorApi);
    api.buscarPorCpf('529.982.247-25').subscribe();
    api.restaurar(3).subscribe();
    http.expectOne({ method: 'GET', url: '/api/tutores/cpf/52998224725' }).flush({});
    http.expectOne({ method: 'POST', url: '/api/tutores/3/restaurar' }).flush({});
  });

  it('PetApi aninha as rotas sob o tutor', () => {
    const api = TestBed.inject(PetApi);
    api.atualizar(1, 2, { nome: 'Rex', especie: 'Cachorro' }).subscribe();
    const req = http.expectOne({ method: 'PUT', url: '/api/tutores/1/pets/2' });
    expect(req.request.body).toEqual({ nome: 'Rex', especie: 'Cachorro' });
    req.flush({});
  });

  it('ServicoApi exclui por id', () => {
    TestBed.inject(ServicoApi).excluir(5).subscribe();
    http.expectOne({ method: 'DELETE', url: '/api/servicos/5' }).flush(null);
  });

  it('AgendamentoApi lista os agendamentos do pet paginados', () => {
    TestBed.inject(AgendamentoApi).listar(7, { pagina: 1 }).subscribe();
    const req = http.expectOne((r) => r.url === '/api/pets/7/agendamentos');
    expect(req.request.params.get('pagina')).toBe('1');
    req.flush({ itens: [], pagina: 1, tamanho: 10, total: 0, totalPaginas: 0 });
  });

  it('AgendamentoApi cancela via DELETE sob o pet', () => {
    TestBed.inject(AgendamentoApi).cancelar(7, 9).subscribe();
    http.expectOne({ method: 'DELETE', url: '/api/pets/7/agendamentos/9' }).flush(null);
  });
});

describe('mensagemDeErro', () => {
  const erro = (status: number, error: unknown) => new HttpErrorResponse({ status, error });

  it('usa o campo "mensagem" dos erros de domínio', () => {
    expect(mensagemDeErro(erro(409, { mensagem: 'E-mail já cadastrado.' }))).toBe(
      'E-mail já cadastrado.',
    );
  });

  it('lista os erros de validação por campo', () => {
    const body = { errors: [{ field: 'nome', defaultMessage: 'obrigatório' }] };
    expect(mensagemDeErro(erro(400, body))).toBe('nome: obrigatório');
  });

  it('usa "detail" de ProblemDetail', () => {
    expect(mensagemDeErro(erro(400, { detail: 'Invalid request content.' }))).toBe(
      'Invalid request content.',
    );
  });

  it('avisa quando a API está fora do ar', () => {
    expect(mensagemDeErro(erro(0, null))).toContain('Não foi possível conectar');
  });

  it('tem mensagem padrão para 404 sem corpo', () => {
    expect(mensagemDeErro(erro(404, null))).toBe('Registro não encontrado.');
  });

  it('trata erros que não são HTTP', () => {
    expect(mensagemDeErro(new Error('x'))).toBe('Erro inesperado.');
  });
});
