import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideTestEnv } from '../../../../testing/providers';
import { TutorApi } from '../../../core/api';
import { Pagina, Tutor } from '../../../core/models';
import { ATRASO_BUSCA_MS } from '../../../shared/listagem/listagem-paginada';
import { ToastService } from '../../../shared/toast/toast.service';
import { TutorList } from './tutor-list';

const tutor = (id: number, nome: string, rua: string): Tutor => ({
  id,
  nome,
  cpf: '52998224725',
  email: `${nome.toLowerCase()}@test.com`,
  telefone: '11987654321',
  endereco: {
    cep: '01001-000',
    logradouro: rua,
    numero: '10',
    bairro: 'Centro',
    cidade: 'São Paulo',
    estado: 'SP',
  },
});

const pagina = (itens: Tutor[], total = itens.length, p = 0): Pagina<Tutor> => ({
  itens,
  pagina: p,
  tamanho: 10,
  total,
  totalPaginas: Math.ceil(total / 10),
});

describe('TutorList', () => {
  let fixture: ComponentFixture<TutorList>;
  let api: jest.Mocked<Pick<TutorApi, 'listar' | 'excluir'>>;
  const toast = { sucesso: jest.fn(), erro: jest.fn() };

  beforeEach(async () => {
    api = {
      listar: jest
        .fn()
        .mockReturnValue(
          of(
            pagina([tutor(1, 'Ana', 'Rua das Flores'), tutor(2, 'Bruno', 'Avenida Paulista')], 12),
          ),
        ),
      excluir: jest.fn().mockReturnValue(of(undefined)),
    };
    await TestBed.configureTestingModule({
      imports: [TutorList],
      providers: [
        provideTestEnv(),
        { provide: TutorApi, useValue: api },
        { provide: ToastService, useValue: toast },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(TutorList);
    fixture.detectChanges();
    // A paginação do nz-table só reflete o total vindo da API no ciclo seguinte.
    fixture.detectChanges();
  });

  const el = () => fixture.nativeElement as HTMLElement;
  const linhas = () => el().querySelectorAll('tbody tr:not(.ant-table-placeholder)');

  it('carrega a primeira página com 10 itens e exibe CPF, telefone e endereço', () => {
    expect(api.listar).toHaveBeenCalledWith({ pagina: 0, tamanho: 10, busca: '' });
    expect(linhas().length).toBe(2);
    expect(linhas()[0].textContent).toContain('529.982.247-25');
    expect(linhas()[0].textContent).toContain('(11) 98765-4321');
    expect(linhas()[0].textContent).toContain('Rua das Flores, 10');
  });

  it('pagina no servidor ao trocar de página', async () => {
    // O nz-table repassa o total à paginação com debounceTime (timer real).
    await new Promise((r) => setTimeout(r, 50));
    fixture.detectChanges();
    const itens = Array.from(el().querySelectorAll<HTMLElement>('li.ant-pagination-item'));
    expect(itens.map((i) => i.textContent?.trim())).toEqual(['1', '2']);
    itens[1].click();
    fixture.detectChanges();
    expect(api.listar).toHaveBeenLastCalledWith({ pagina: 1, tamanho: 10, busca: '' });
  });

  it('busca no servidor só após o usuário parar de digitar', async () => {
    api.listar.mockClear();
    const input = el().querySelector<HTMLInputElement>('input[type=search]')!;
    input.value = 'paulista';
    input.dispatchEvent(new Event('input'));
    expect(api.listar).not.toHaveBeenCalled();

    await new Promise((r) => setTimeout(r, ATRASO_BUSCA_MS + 50));
    expect(api.listar).toHaveBeenCalledWith({ pagina: 0, tamanho: 10, busca: 'paulista' });
  });

  // O nz-dropdown agenda a abertura fora da zona do Angular (auditTime), então usamos espera real.
  const esperar = async (ms = 250) => {
    await new Promise((r) => setTimeout(r, ms));
    fixture.detectChanges();
    await fixture.whenStable();
  };

  const abrirMenuEExcluir = async () => {
    linhas()[0].querySelector<HTMLButtonElement>('button[aria-label="Ações"]')!.click();
    await esperar();
    const itens = Array.from(document.querySelectorAll<HTMLElement>('.ant-dropdown-menu-item'));
    expect(itens.map((i) => i.textContent?.trim())).toEqual(['Ver detalhes', 'Editar', 'Excluir']);
    itens[2].click();
    await esperar();
  };

  it('exclui pelo menu de ações após confirmar no modal e recarrega a página', async () => {
    await abrirMenuEExcluir();
    expect(document.querySelector('.ant-modal-confirm-title')?.textContent).toContain(
      'Excluir o tutor Ana?',
    );
    api.listar.mockClear();

    document
      .querySelector<HTMLButtonElement>('.ant-modal-confirm-btns .ant-btn-dangerous')!
      .click();
    await esperar();

    expect(api.excluir).toHaveBeenCalledWith(1);
    expect(toast.sucesso).toHaveBeenCalledWith('Tutor excluído.');
    expect(api.listar).toHaveBeenCalledWith({ pagina: 0, tamanho: 10, busca: '' });
  });

  it('não exclui se o usuário cancelar no modal', async () => {
    await abrirMenuEExcluir();
    document.querySelectorAll<HTMLButtonElement>('.ant-modal-confirm-btns .ant-btn')[0].click();
    await esperar();
    expect(api.excluir).not.toHaveBeenCalled();
  });
});
