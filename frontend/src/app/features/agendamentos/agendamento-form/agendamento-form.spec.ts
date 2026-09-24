import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { provideTestEnv } from '../../../../testing/providers';
import { AgendamentoApi, ServicoApi } from '../../../core/api';
import { ATRASO_BUSCA_MS } from '../../../shared/listagem/listagem-paginada';
import { ToastService } from '../../../shared/toast/toast.service';
import { AgendamentoForm } from './agendamento-form';

const paginaServicos = (itens: { id: number; nome: string; preco: number }[]) => ({
  itens,
  pagina: 0,
  tamanho: 10,
  total: itens.length,
  totalPaginas: 1,
});

describe('AgendamentoForm', () => {
  let fixture: ComponentFixture<AgendamentoForm>;
  let api: jest.Mocked<Pick<AgendamentoApi, 'buscar' | 'criar' | 'atualizar'>>;
  const servicoApi = { listar: jest.fn() };

  beforeEach(async () => {
    api = {
      buscar: jest.fn(),
      criar: jest.fn().mockReturnValue(of({})),
      atualizar: jest.fn(),
    };
    servicoApi.listar.mockReset().mockReturnValue(
      of(
        paginaServicos([
          { id: 1, nome: 'Banho', preco: 50 },
          { id: 2, nome: 'Tosa', preco: 30.5 },
        ]),
      ),
    );
    await TestBed.configureTestingModule({
      imports: [AgendamentoForm],
      providers: [
        provideTestEnv(),
        { provide: AgendamentoApi, useValue: api },
        { provide: ServicoApi, useValue: servicoApi },
        { provide: ToastService, useValue: { sucesso: jest.fn(), erro: jest.fn() } },
      ],
    }).compileComponents();
    jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  });

  const criar = (id?: number) => {
    fixture = TestBed.createComponent(AgendamentoForm);
    fixture.componentRef.setInput('tutorId', 1);
    fixture.componentRef.setInput('petId', 7);
    if (id) fixture.componentRef.setInput('id', id);
    fixture.detectChanges();
  };

  const el = () => fixture.nativeElement as HTMLElement;
  interface Interno {
    form: AgendamentoForm['form'];
    opcoes: () => { id: number; nome: string }[];
    buscarServicos: (t: string) => void;
  }
  const comp = () => fixture.componentInstance as unknown as Interno;
  const submeter = () => {
    el().querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  };

  it('carrega só a primeira página de serviços como opções', fakeAsync(() => {
    criar();
    tick();
    expect(servicoApi.listar).toHaveBeenCalledTimes(1);
    expect(servicoApi.listar).toHaveBeenCalledWith({ busca: '' });
    expect(
      comp()
        .opcoes()
        .map((o) => o.nome),
    ).toEqual(['Banho', 'Tosa']);
  }));

  it('busca serviços no servidor e mantém os já selecionados nas opções', fakeAsync(() => {
    criar();
    comp().form.controls.servicoIds.setValue([1]);
    servicoApi.listar.mockReturnValue(
      of(paginaServicos([{ id: 3, nome: 'Hidratação', preco: 80 }])),
    );

    comp().buscarServicos('hidra');
    tick(ATRASO_BUSCA_MS);

    expect(servicoApi.listar).toHaveBeenLastCalledWith({ busca: 'hidra' });
    expect(
      comp()
        .opcoes()
        .map((o) => o.nome),
    ).toEqual(['Banho', 'Hidratação']);
  }));

  it('soma o total dos serviços selecionados', () => {
    criar();
    comp().form.controls.servicoIds.setValue([1, 2]);
    fixture.detectChanges();
    expect(el().querySelector('.total')!.textContent!.replace(/\s/g, ' ')).toContain('R$ 80,50');
  });

  it('exige ao menos um serviço', () => {
    criar();
    comp().form.patchValue({ dataHora: new Date(2099, 11, 31, 10, 0) });
    submeter();
    expect(api.criar).not.toHaveBeenCalled();
    expect(el().textContent).toContain('Selecione ao menos um serviço.');
  });

  it('cria o agendamento enviando LocalDateTime', () => {
    criar();
    comp().form.setValue({
      dataHora: new Date(2099, 11, 31, 10, 30),
      observacoes: 'Levar coleira',
      servicoIds: [2],
    });
    submeter();

    expect(api.criar).toHaveBeenCalledWith(7, {
      dataHora: '2099-12-31T10:30:00',
      observacoes: 'Levar coleira',
      servicoIds: [2],
    });
    expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith(['/tutores', 1, 'pets', 7]);
  });

  it('carrega o agendamento existente ao reagendar, com os serviços escolhidos', () => {
    api.buscar.mockReturnValue(
      of({
        id: 3,
        petId: 7,
        dataHora: '2099-01-02T09:00:00',
        observacoes: null,
        status: 'AGENDADO',
        valorTotal: 45,
        servicos: [{ servicoId: 9, nome: 'Corte de unhas', precoCobrado: 45 }],
      }),
    );
    criar(3);

    expect(api.buscar).toHaveBeenCalledWith(7, 3);
    expect(comp().form.value.dataHora).toEqual(new Date(2099, 0, 2, 9, 0));
    expect(comp().form.value.servicoIds).toEqual([9]);
    expect(comp().opcoes()[0]).toMatchObject({ id: 9, nome: 'Corte de unhas' });
    expect(el().querySelector('.total')!.textContent!.replace(/\s/g, ' ')).toContain('R$ 45,00');
  });
});
