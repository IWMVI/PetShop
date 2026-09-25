import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideTestEnv } from '../../../../testing/providers';
import { FuncionarioApi } from '../../../core/api';
import { Funcionario, Pagina } from '../../../core/models';
import { ATRASO_BUSCA_MS } from '../../../shared/listagem/listagem-paginada';
import { ToastService } from '../../../shared/toast/toast.service';
import { FuncionarioList } from './funcionario-list';

const funcionario = (id: number, nome: string, cargo: Funcionario['cargo']): Funcionario => ({
  id,
  nome,
  cpf: '52998224725',
  cargo,
  telefone: '11988887777',
});

const pagina = (itens: Funcionario[]): Pagina<Funcionario> => ({
  itens,
  pagina: 0,
  tamanho: 10,
  total: itens.length,
  totalPaginas: 1,
});

describe('FuncionarioList', () => {
  let fixture: ComponentFixture<FuncionarioList>;
  let api: jest.Mocked<Pick<FuncionarioApi, 'listar' | 'excluir'>>;
  const toast = { sucesso: jest.fn(), erro: jest.fn() };

  beforeEach(async () => {
    api = {
      listar: jest
        .fn()
        .mockReturnValue(
          of(pagina([funcionario(1, 'Ana', 'VETERINARIO'), funcionario(2, 'Bia', 'TOSADOR')])),
        ),
      excluir: jest.fn().mockReturnValue(of(undefined)),
    };
    await TestBed.configureTestingModule({
      imports: [FuncionarioList],
      providers: [
        provideTestEnv(),
        { provide: FuncionarioApi, useValue: api },
        { provide: ToastService, useValue: toast },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(FuncionarioList);
    fixture.detectChanges();
    fixture.detectChanges();
  });

  const el = () => fixture.nativeElement as HTMLElement;
  const linhas = () => el().querySelectorAll('tbody tr:not(.ant-table-placeholder)');

  const esperar = async (ms = 250) => {
    await new Promise((r) => setTimeout(r, ms));
    fixture.detectChanges();
    await fixture.whenStable();
  };

  it('carrega a primeira página e exibe cargo, CPF e telefone formatados', () => {
    expect(api.listar).toHaveBeenCalledWith({ pagina: 0, tamanho: 10, busca: '' });
    expect(linhas().length).toBe(2);
    expect(linhas()[0].textContent).toContain('Veterinário(a)');
    expect(linhas()[0].textContent).toContain('529.982.247-25');
    expect(linhas()[0].textContent).toContain('(11) 98888-7777');
    expect(linhas()[1].textContent).toContain('Tosador(a)');
  });

  it('busca no servidor só após o usuário parar de digitar', async () => {
    api.listar.mockClear();
    const input = el().querySelector<HTMLInputElement>('input[type=search]')!;
    input.value = 'ana';
    input.dispatchEvent(new Event('input'));
    expect(api.listar).not.toHaveBeenCalled();

    await new Promise((r) => setTimeout(r, ATRASO_BUSCA_MS + 50));
    expect(api.listar).toHaveBeenCalledWith({ pagina: 0, tamanho: 10, busca: 'ana' });
  });

  it('exclui pelo menu de ações após confirmar no modal', async () => {
    linhas()[0].querySelector<HTMLButtonElement>('button[aria-label="Ações"]')!.click();
    await esperar();
    const itens = Array.from(document.querySelectorAll<HTMLElement>('.ant-dropdown-menu-item'));
    expect(itens.map((i) => i.textContent?.trim())).toEqual(['Editar', 'Excluir']);
    itens[1].click();
    await esperar();
    expect(document.querySelector('.ant-modal-confirm-title')?.textContent).toContain(
      'Excluir o funcionário Ana?',
    );

    document
      .querySelector<HTMLButtonElement>('.ant-modal-confirm-btns .ant-btn-dangerous')!
      .click();
    await esperar();

    expect(api.excluir).toHaveBeenCalledWith(1);
    expect(toast.sucesso).toHaveBeenCalledWith('Funcionário excluído.');
  });
});
