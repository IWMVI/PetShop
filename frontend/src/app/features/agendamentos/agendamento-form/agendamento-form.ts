import {
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  input,
  numberAttribute,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import {
  Subject,
  catchError,
  debounceTime,
  distinctUntilChanged,
  map,
  of,
  startWith,
  switchMap,
  tap,
} from 'rxjs';
import { AgendamentoApi, ServicoApi, mensagemDeErro } from '../../../core/api';
import { AgendamentoRequest } from '../../../core/models';
import { formatarMoeda, paraLocalDateTime, vazioParaNull } from '../../../shared/format';
import { ATRASO_BUSCA_MS } from '../../../shared/listagem/listagem-paginada';
import { ItemTrilha, Pagina } from '../../../shared/pagina/pagina';
import { ToastService } from '../../../shared/toast/toast.service';

/** Serviço como opção do select (nome e preço para exibir e somar o total). */
interface OpcaoServico {
  id: number;
  nome: string;
  preco: number;
}

@Component({
  selector: 'app-agendamento-form',
  imports: [
    Pagina,
    ReactiveFormsModule,
    RouterLink,
    NzButtonModule,
    NzCardModule,
    NzDatePickerModule,
    NzFormModule,
    NzInputModule,
    NzSelectModule,
  ],
  templateUrl: './agendamento-form.html',
  styleUrl: './agendamento-form.scss',
})
export class AgendamentoForm implements OnInit {
  readonly tutorId = input.required({ transform: numberAttribute });
  readonly petId = input.required({ transform: numberAttribute });
  readonly id = input(undefined, { transform: numberAttribute });

  protected readonly titulo = computed(() => (this.id() ? 'Reagendar' : 'Novo agendamento'));
  protected readonly trilha = computed<ItemTrilha[]>(() => [
    { rotulo: 'Tutores', link: '/tutores' },
    { rotulo: 'Tutor', link: ['/tutores', this.tutorId()] },
    { rotulo: 'Pet', link: this.voltar() },
    { rotulo: this.id() ? 'Reagendar' : 'Novo agendamento' },
  ]);

  private readonly api = inject(AgendamentoApi);
  private readonly servicoApi = inject(ServicoApi);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly salvando = signal(false);
  protected readonly moeda = formatarMoeda;

  protected readonly form = inject(FormBuilder).group({
    dataHora: [null as Date | null, Validators.required],
    observacoes: ['', Validators.maxLength(1000)],
    servicoIds: [[] as number[], Validators.required],
  });

  /**
   * Opções do select: só a primeira página de serviços é carregada, e a busca é feita
   * no servidor conforme o usuário digita. Os serviços já escolhidos ficam guardados
   * em `conhecidos` para continuar exibindo nome e preço quando saem do resultado da busca.
   */
  private readonly busca$ = new Subject<string>();
  protected readonly buscandoServicos = signal(false);
  private readonly conhecidos = signal(new Map<number, OpcaoServico>());
  private readonly resultadoBusca = signal<OpcaoServico[]>([]);

  protected readonly opcoes = computed(() => {
    const selecionados = this.selecionados()
      .map((id) => this.conhecidos().get(id))
      .filter((o): o is OpcaoServico => !!o);
    const ids = new Set(selecionados.map((o) => o.id));
    return [...selecionados, ...this.resultadoBusca().filter((o) => !ids.has(o.id))];
  });

  private readonly selecionados = toSignal(
    this.form.controls.servicoIds.valueChanges.pipe(map((ids) => ids ?? [])),
    { initialValue: [] as number[] },
  );

  protected readonly total = computed(() =>
    this.selecionados().reduce((acc, id) => acc + Number(this.conhecidos().get(id)?.preco ?? 0), 0),
  );

  /** Impede escolher dias anteriores a hoje no calendário. */
  protected readonly dataDesabilitada = (d: Date) => {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    return d < hoje;
  };

  voltar() {
    return ['/tutores', this.tutorId(), 'pets', this.petId()];
  }

  ngOnInit() {
    this.buscarServicosAoDigitar();

    const id = this.id();
    if (!id) return;
    this.api.buscar(this.petId(), id).subscribe({
      next: (a) => {
        this.lembrar(
          a.servicos.map((s) => ({
            id: s.servicoId,
            nome: s.nome ?? `#${s.servicoId}`,
            preco: s.precoCobrado,
          })),
        );
        this.form.patchValue({
          dataHora: new Date(a.dataHora),
          observacoes: a.observacoes ?? '',
          servicoIds: a.servicos.map((s) => s.servicoId),
        });
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }

  buscarServicos(termo: string) {
    this.busca$.next(termo);
  }

  salvar() {
    if (this.form.invalid) {
      for (const c of Object.values(this.form.controls)) {
        c.markAsDirty();
        c.updateValueAndValidity({ onlySelf: true });
      }
      return;
    }
    const v = this.form.getRawValue();
    const body: AgendamentoRequest = {
      dataHora: paraLocalDateTime(v.dataHora!),
      observacoes: vazioParaNull(v.observacoes),
      servicoIds: v.servicoIds!,
    };
    const id = this.id();
    this.salvando.set(true);
    (id
      ? this.api.atualizar(this.petId(), id, body)
      : this.api.criar(this.petId(), body)
    ).subscribe({
      next: () => {
        this.toast.sucesso(id ? 'Agendamento atualizado.' : 'Agendamento criado.');
        this.router.navigate(this.voltar());
      },
      error: (e) => {
        this.salvando.set(false);
        this.toast.erro(mensagemDeErro(e));
      },
    });
  }

  private buscarServicosAoDigitar() {
    this.busca$
      .pipe(
        debounceTime(ATRASO_BUSCA_MS),
        startWith(''),
        distinctUntilChanged(),
        tap(() => this.buscandoServicos.set(true)),
        switchMap((busca) =>
          this.servicoApi.listar({ busca }).pipe(
            map((p) => p.itens.map(({ id, nome, preco }) => ({ id, nome, preco }))),
            catchError((e) => {
              this.toast.erro(mensagemDeErro(e));
              return of([] as OpcaoServico[]);
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((opcoes) => {
        this.lembrar(opcoes);
        this.resultadoBusca.set(opcoes);
        this.buscandoServicos.set(false);
      });
  }

  private lembrar(opcoes: OpcaoServico[]) {
    this.conhecidos.update((mapa) => {
      const novo = new Map(mapa);
      for (const o of opcoes) if (!novo.has(o.id)) novo.set(o.id, o);
      return novo;
    });
  }
}
