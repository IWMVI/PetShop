import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import {
  AgendamentoApi,
  FuncionarioApi,
  HistoricoPetApi,
  PetApi,
  ServicoApi,
  TutorApi,
  mensagemDeErro,
} from './api';

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

describe('FuncionarioApi', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista de forma paginada e envia a busca', () => {
    TestBed.inject(FuncionarioApi).listar({ pagina: 1, busca: ' ana ' }).subscribe();
    const req = http.expectOne((r) => r.method === 'GET' && r.url === '/api/funcionarios');
    expect(req.request.params.get('pagina')).toBe('1');
    expect(req.request.params.get('busca')).toBe('ana');
    req.flush({ itens: [], pagina: 1, tamanho: 10, total: 0, totalPaginas: 0 });
  });

  it('cria, atualiza e exclui por id', () => {
    const api = TestBed.inject(FuncionarioApi);
    const dados = {
      nome: 'Ana',
      cpf: '52998224725',
      cargo: 'TOSADOR' as const,
      telefone: '11988887777',
    };
    api.criar(dados).subscribe();
    api.atualizar(4, dados).subscribe();
    api.excluir(4).subscribe();
    expect(http.expectOne({ method: 'POST', url: '/api/funcionarios' }).request.body).toEqual(
      dados,
    );
    http.expectOne({ method: 'PUT', url: '/api/funcionarios/4' }).flush({});
    http.expectOne({ method: 'DELETE', url: '/api/funcionarios/4' }).flush(null);
  });
});

describe('HistoricoPetApi', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista os eventos do pet', () => {
    TestBed.inject(HistoricoPetApi).listar(7).subscribe();
    http.expectOne({ method: 'GET', url: '/api/pets/7/historico' }).flush([]);
  });

  it('consulta um evento específico', () => {
    TestBed.inject(HistoricoPetApi).buscar(7, 3).subscribe();
    http.expectOne({ method: 'GET', url: '/api/pets/7/historico/3' }).flush({});
  });

  it('registra um evento sob o pet', () => {
    const evento = {
      tipoEvento: 'VACINACAO' as const,
      descricao: 'V10',
      dataEvento: '2026-01-10T14:30:00',
      funcionarioId: null,
    };
    TestBed.inject(HistoricoPetApi).registrar(7, evento).subscribe();
    const req = http.expectOne({ method: 'POST', url: '/api/pets/7/historico' });
    expect(req.request.body).toEqual(evento);
    req.flush({});
  });

  it('não oferece alteração nem exclusão, pois o histórico é imutável', () => {
    const api = TestBed.inject(HistoricoPetApi) as unknown as Record<string, unknown>;
    for (const metodo of ['atualizar', 'excluir', 'editar', 'cancelar']) {
      expect(api[metodo]).toBeUndefined();
    }
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
