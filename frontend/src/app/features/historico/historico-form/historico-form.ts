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
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzGridModule } from 'ng-zorro-antd/grid';
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
import { FuncionarioApi, HistoricoPetApi, mensagemDeErro } from '../../../core/api';
import { HistoricoPetRequest, TipoEvento } from '../../../core/models';
import { paraLocalDateTime } from '../../../shared/format';
import { ATRASO_BUSCA_MS } from '../../../shared/listagem/listagem-paginada';
import { ItemTrilha, Pagina } from '../../../shared/pagina/pagina';
import { CARGOS, TIPOS_EVENTO, opcoes } from '../../../shared/rotulos/rotulos';
import { ToastService } from '../../../shared/toast/toast.service';

/** Funcionário como opção do select (nome e cargo já formatados). */
interface OpcaoFuncionario {
  id: number;
  rotulo: string;
}

/** A API recusa eventos no futuro; o calendário só bloqueia dias, então validamos o horário. */
function dataNaoFutura(control: AbstractControl<Date | null>): ValidationErrors | null {
  const data = control.value;
  return data && data.getTime() > Date.now() ? { futura: true } : null;
}

@Component({
  selector: 'app-historico-form',
  imports: [
    Pagina,
    ReactiveFormsModule,
    RouterLink,
    NzButtonModule,
    NzCardModule,
    NzDatePickerModule,
    NzFormModule,
    NzGridModule,
    NzInputModule,
    NzSelectModule,
  ],
  templateUrl: './historico-form.html',
  styleUrl: './historico-form.scss',
})
export class HistoricoForm implements OnInit {
  readonly tutorId = input.required({ transform: numberAttribute });
  readonly petId = input.required({ transform: numberAttribute });

  protected readonly trilha = computed<ItemTrilha[]>(() => [
    { rotulo: 'Tutores', link: '/tutores' },
    { rotulo: 'Tutor', link: ['/tutores', this.tutorId()] },
    { rotulo: 'Pet', link: this.voltar() },
    { rotulo: 'Novo evento' },
  ]);

  private readonly api = inject(HistoricoPetApi);
  private readonly funcionarioApi = inject(FuncionarioApi);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly salvando = signal(false);
  protected readonly tipos = opcoes(TIPOS_EVENTO);

  protected readonly form = inject(FormBuilder).group({
    tipoEvento: [null as TipoEvento | null, Validators.required],
    dataEvento: [null as Date | null, [Validators.required, dataNaoFutura]],
    funcionarioId: [null as number | null],
    descricao: ['', [Validators.required, Validators.pattern(/\S/)]],
  });

  /** Só a primeira página de funcionários é carregada; a busca acontece no servidor. */
  private readonly busca$ = new Subject<string>();
  protected readonly buscandoFuncionarios = signal(false);
  private readonly resultadoBusca = signal<OpcaoFuncionario[]>([]);
  private readonly escolhido = signal<OpcaoFuncionario | null>(null);

  /** O funcionário escolhido continua na lista mesmo quando some do resultado da busca. */
  protected readonly funcionarios = computed(() => {
    const escolhido = this.escolhido();
    const resto = this.resultadoBusca().filter((o) => o.id !== escolhido?.id);
    return escolhido ? [escolhido, ...resto] : resto;
  });

  /** Impede escolher dias posteriores a hoje no calendário. */
  protected readonly dataDesabilitada = (d: Date) => {
    const fimDoDia = new Date();
    fimDoDia.setHours(23, 59, 59, 999);
    return d > fimDoDia;
  };

  voltar() {
    return ['/tutores', this.tutorId(), 'pets', this.petId()];
  }

  ngOnInit() {
    this.buscarFuncionariosAoDigitar();
    this.form.controls.funcionarioId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((id) => {
        if (id == null) this.escolhido.set(null);
        else this.escolhido.set(this.resultadoBusca().find((o) => o.id === id) ?? this.escolhido());
      });
  }

  buscarFuncionarios(termo: string) {
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
    const body: HistoricoPetRequest = {
      tipoEvento: v.tipoEvento!,
      dataEvento: paraLocalDateTime(v.dataEvento!),
      funcionarioId: v.funcionarioId,
      descricao: v.descricao!.trim(),
    };
    this.salvando.set(true);
    this.api.registrar(this.petId(), body).subscribe({
      next: () => {
        this.toast.sucesso('Evento registrado no histórico.');
        this.router.navigate(this.voltar());
      },
      error: (e) => {
        this.salvando.set(false);
        this.toast.erro(mensagemDeErro(e));
      },
    });
  }

  private buscarFuncionariosAoDigitar() {
    this.busca$
      .pipe(
        debounceTime(ATRASO_BUSCA_MS),
        startWith(''),
        distinctUntilChanged(),
        tap(() => this.buscandoFuncionarios.set(true)),
        switchMap((busca) =>
          this.funcionarioApi.listar({ busca }).pipe(
            map((p) =>
              p.itens.map((f) => ({ id: f.id, rotulo: `${f.nome} · ${CARGOS[f.cargo]}` })),
            ),
            catchError((e) => {
              this.toast.erro(mensagemDeErro(e));
              return of([] as OpcaoFuncionario[]);
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((opcoesBusca) => {
        this.resultadoBusca.set(opcoesBusca);
        this.buscandoFuncionarios.set(false);
      });
  }
}
