import { Page, Route } from '@playwright/test';

interface Registro {
  id: number;
  [campo: string]: unknown;
}

/** Monta uma página no formato da API ({ itens, pagina, tamanho, total, totalPaginas }). */
function paginar<T>(lista: T[], url: URL) {
  const pagina = Number(url.searchParams.get('pagina') ?? 0);
  const tamanho = Number(url.searchParams.get('tamanho') ?? 10);
  return {
    itens: lista.slice(pagina * tamanho, (pagina + 1) * tamanho),
    pagina,
    tamanho,
    total: lista.length,
    totalPaginas: Math.ceil(lista.length / tamanho),
  };
}

/** Filtra pelo termo de busca (?busca=) nos campos de texto indicados. */
function buscar(lista: Registro[], url: URL, campos: (r: Registro) => unknown[]) {
  const termo = url.searchParams.get('busca')?.toLowerCase();
  if (!termo) return lista;
  const digitos = termo.replace(/\D/g, '');
  return lista.filter((r) =>
    campos(r).some((v) => {
      const texto = String(v ?? '').toLowerCase();
      return texto.includes(termo) || (digitos.length > 0 && texto.includes(digitos));
    }),
  );
}

/**
 * API do PetShop simulada em memória, interceptando as chamadas a /api/**.
 * Reproduz as rotas e códigos de status do back-end Spring.
 */
export class MockApi {
  private seq = 100;
  readonly tutores: Registro[] = [];
  readonly pets = new Map<number, Registro[]>();
  readonly servicos: Registro[] = [];
  readonly agendamentos = new Map<number, Registro[]>();

  static async instalar(page: Page) {
    const api = new MockApi();
    await page.route('**/api/**', (route) => api.responder(route));
    // Consulta de CEP (ViaCEP): 01001-000 existe; qualquer outro CEP não é encontrado.
    await page.route('https://viacep.com.br/**', (route) =>
      route.fulfill({
        json: route.request().url().includes('/01001000/')
          ? {
              cep: '01001-000',
              logradouro: 'Praça da Sé',
              complemento: 'lado ímpar',
              bairro: 'Sé',
              localidade: 'São Paulo',
              uf: 'SP',
            }
          : { erro: 'true' },
      }),
    );
    return api;
  }

  tutor(dados: Partial<Registro> = {}) {
    const t = {
      id: ++this.seq,
      nome: 'Ana Souza',
      cpf: null as string | null,
      email: `ana${this.seq}@test.com`,
      telefone: '11987654321',
      endereco: {
        cep: '01001-000',
        logradouro: 'Praça da Sé',
        numero: '1',
        complemento: null,
        bairro: 'Sé',
        cidade: 'São Paulo',
        estado: 'SP',
      },
      ...dados,
    };
    this.tutores.push(t);
    this.pets.set(t.id, []);
    return t;
  }

  pet(tutorId: number, dados: Partial<Registro> = {}) {
    const p = {
      id: ++this.seq,
      nome: 'Rex',
      especie: 'Cachorro',
      raca: 'Labrador',
      idade: 3,
      peso: 25.5,
      ...dados,
    };
    this.pets.get(tutorId)!.push(p);
    this.agendamentos.set(p.id, []);
    return p;
  }

  servico(dados: Partial<Registro> = {}) {
    const s = {
      id: ++this.seq,
      nome: 'Banho',
      descricao: null,
      preco: 50,
      tempoEstimadoMinutos: 40,
      ...dados,
    };
    this.servicos.push(s);
    return s;
  }

  private async responder(route: Route) {
    const req = route.request();
    const url = new URL(req.url());
    const partes = url.pathname.replace(/^\/api\//, '').split('/');
    const metodo = req.method();
    const body = req.postDataJSON?.() as Record<string, unknown> | undefined;
    const json = (status: number, data?: unknown) =>
      route.fulfill({
        status,
        contentType: 'application/json',
        body: data === undefined ? '' : JSON.stringify(data),
      });
    const naoEncontrado = () => json(404, { mensagem: 'Registro não encontrado.' });

    // Saúde da API (Spring Boot Actuator), usada pelo indicador de status do menu.
    if (partes[0] === 'actuator' && partes[1] === 'health') {
      return json(200, { status: 'UP' });
    }

    // /tutores[/id][/pets[/petId]] — tutores excluídos ficam com "excluido: true" (exclusão lógica)
    if (partes[0] === 'tutores') {
      const ativos = this.tutores.filter((t) => !t['excluido']);
      if (partes[1] === 'cpf') {
        const t = this.tutores.find((x) => x['cpf'] === partes[2]);
        if (!t) return naoEncontrado();
        return json(200, {
          id: t.id,
          nome: t['nome'],
          email: t['email'],
          excluido: !!t['excluido'],
        });
      }
      if (partes[2] === 'restaurar' && metodo === 'POST') {
        const t = this.tutores.find((x) => x.id === Number(partes[1]));
        if (!t) return naoEncontrado();
        t['excluido'] = false;
        return json(200, t);
      }
      if (metodo === 'DELETE' && partes[1] && !partes[2]) {
        const t = ativos.find((x) => x.id === Number(partes[1]));
        if (!t) return naoEncontrado();
        t['excluido'] = true;
        return json(204);
      }
      const tutorId = Number(partes[1]);
      if (partes[2] === 'pets') {
        const pets = this.pets.get(tutorId);
        if (!pets) return naoEncontrado();
        return this.crud(route, pets, partes[3], metodo, body, (dados) => {
          const p = { id: ++this.seq, ...dados };
          this.agendamentos.set(p.id, []);
          return p;
        });
      }
      if (metodo === 'POST' && !partes[1]) {
        if (this.tutores.some((t) => t['email'] === body?.['email'])) {
          return json(409, { mensagem: 'E-mail já cadastrado.' });
        }
        if (this.tutores.some((t) => t['cpf'] === body?.['cpf'])) {
          return json(409, { mensagem: 'CPF já cadastrado.' });
        }
        const t = this.tutor(body);
        return json(201, t);
      }
      if (metodo === 'GET' && !partes[1]) {
        const filtrados = buscar(ativos, url, (t) => [
          t['nome'],
          t['email'],
          (t['endereco'] as Record<string, unknown>)['logradouro'],
          t['cpf'],
        ]);
        return json(200, paginar(filtrados, url));
      }
      return this.crud(route, ativos, partes[1], metodo, body);
    }

    if (partes[0] === 'servicos') {
      if (metodo === 'GET' && !partes[1]) {
        return json(
          200,
          paginar(
            buscar(this.servicos, url, (sv) => [sv['nome'], sv['descricao']]),
            url,
          ),
        );
      }
      return this.crud(route, this.servicos, partes[1], metodo, body);
    }

    // /pets/{petId}/agendamentos[/id]
    if (partes[0] === 'pets' && partes[2] === 'agendamentos') {
      const lista = this.agendamentos.get(Number(partes[1]));
      if (!lista) return naoEncontrado();
      const montar = (dados: Record<string, unknown>, id: number) => {
        const ids = dados['servicoIds'] as number[];
        const servicos = ids.map((sid) => {
          const servico = this.servicos.find((s) => s.id === sid)!;
          return { servicoId: sid, nome: servico['nome'], precoCobrado: servico['preco'] };
        });
        return {
          id,
          petId: Number(partes[1]),
          dataHora: dados['dataHora'],
          observacoes: dados['observacoes'],
          status: 'AGENDADO',
          valorTotal: servicos.reduce((acc, s) => acc + Number(s.precoCobrado), 0),
          servicos,
        };
      };
      if (metodo === 'DELETE') {
        // Como no back-end: cancelar marca CANCELADO e faz a exclusão lógica (some da listagem).
        const i = lista.findIndex((x) => x.id === Number(partes[3]));
        if (i < 0) return naoEncontrado();
        lista[i]['status'] = 'CANCELADO';
        lista.splice(i, 1);
        return json(204);
      }
      if (metodo === 'GET' && !partes[3]) {
        const ordenados = [...lista].sort((a, b) =>
          String(a['dataHora']).localeCompare(String(b['dataHora'])),
        );
        return json(200, paginar(ordenados, url));
      }
      if (metodo === 'POST') {
        const a = montar(body!, ++this.seq);
        lista.push(a);
        return json(201, a);
      }
      if (metodo === 'PUT') {
        const i = lista.findIndex((x) => x.id === Number(partes[3]));
        if (i < 0) return naoEncontrado();
        lista[i] = montar(body!, lista[i].id);
        return json(200, lista[i]);
      }
      return this.crud(route, lista, partes[3], metodo, body);
    }

    return naoEncontrado();
  }

  private crud(
    route: Route,
    lista: Registro[],
    idTexto: string | undefined,
    metodo: string,
    body: Record<string, unknown> | undefined,
    criar: (dados: Record<string, unknown>) => Registro = (dados) => ({ id: ++this.seq, ...dados }),
  ) {
    const json = (status: number, data?: unknown) =>
      route.fulfill({
        status,
        contentType: 'application/json',
        body: data === undefined ? '' : JSON.stringify(data),
      });
    if (!idTexto) {
      if (metodo === 'GET') return json(200, lista);
      if (metodo === 'POST') {
        const novo = criar(body!);
        lista.push(novo);
        return json(201, novo);
      }
    }
    const i = lista.findIndex((x) => x.id === Number(idTexto));
    if (i < 0) return json(404, { mensagem: 'Registro não encontrado.' });
    if (metodo === 'GET') return json(200, lista[i]);
    if (metodo === 'PUT') {
      lista[i] = { ...lista[i], ...body, id: lista[i].id };
      return json(200, lista[i]);
    }
    if (metodo === 'DELETE') {
      lista.splice(i, 1);
      return json(204);
    }
    return json(405);
  }
}
