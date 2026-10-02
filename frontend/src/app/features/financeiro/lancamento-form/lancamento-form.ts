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
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { FinanceiroApi, mensagemDeErro } from '../../../core/api';
import {
  CategoriaLancamento,
  LancamentoFinanceiroRequest,
  TipoLancamento,
} from '../../../core/models';
import { paraLocalDateTime } from '../../../shared/format';
import { MoedaDirective } from '../../../shared/moeda/moeda.directive';
import { ItemTrilha, Pagina } from '../../../shared/pagina/pagina';
import {
  CATEGORIAS_ENTRADA,
  CATEGORIAS_LANCAMENTO,
  CATEGORIAS_SAIDA,
  TIPOS_LANCAMENTO,
  opcoes,
} from '../../../shared/rotulos/rotulos';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-lancamento-form',
  imports: [
    Pagina,
    ReactiveFormsModule,
    RouterLink,
    MoedaDirective,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzDatePickerModule,
    NzFormModule,
    NzGridModule,
    NzInputModule,
    NzSelectModule,
  ],
  templateUrl: './lancamento-form.html',
  styleUrl: './lancamento-form.scss',
})
export class LancamentoForm implements OnInit {
  readonly id = input(undefined, { transform: numberAttribute });

  protected readonly titulo = computed(() => (this.id() ? 'Editar lançamento' : 'Novo lançamento'));
  protected readonly trilha = computed<ItemTrilha[]>(() => [
    { rotulo: 'Financeiro', link: '/financeiro' },
    { rotulo: this.id() ? 'Editar' : 'Novo' },
  ]);

  private readonly api = inject(FinanceiroApi);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly salvando = signal(false);
  /** Defesa: um lançamento gerado por pagamento não deveria chegar a esta tela editável. */
  protected readonly bloqueado = signal(false);

  protected readonly tipos = opcoes(TIPOS_LANCAMENTO);
  protected readonly categoriasLancamento = CATEGORIAS_LANCAMENTO;

  protected readonly form = inject(FormBuilder).group({
    tipo: [null as TipoLancamento | null, Validators.required],
    categoria: [null as CategoriaLancamento | null, Validators.required],
    descricao: ['', [Validators.required, Validators.maxLength(255)]],
    valor: [null as number | null, [Validators.required, Validators.min(0.01)]],
    data: [null as Date | null, Validators.required],
  });

  private readonly tipoAtual = signal<TipoLancamento | null>(null);

  /** Opções de categoria compatíveis com o tipo escolhido no formulário. */
  protected readonly categorias = computed(() => {
    const lista = this.tipoAtual() === 'SAIDA' ? CATEGORIAS_SAIDA : CATEGORIAS_ENTRADA;
    return lista.map((valor) => ({ valor, rotulo: CATEGORIAS_LANCAMENTO[valor] }));
  });

  ngOnInit() {
    this.form.controls.tipo.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((tipo) => {
        this.tipoAtual.set(tipo);
        const validas = tipo === 'SAIDA' ? CATEGORIAS_SAIDA : CATEGORIAS_ENTRADA;
        const categoriaAtual = this.form.controls.categoria.value;
        if (categoriaAtual && !validas.includes(categoriaAtual)) {
          this.form.controls.categoria.setValue(null);
        }
      });

    const id = this.id();
    if (!id) return;
    this.api.buscar(id).subscribe({
      next: (l) => {
        if (l.pagamentoId != null) {
          this.bloqueado.set(true);
          this.form.disable();
        }
        this.form.patchValue({
          tipo: l.tipo,
          categoria: l.categoria,
          descricao: l.descricao,
          valor: l.valor,
          data: new Date(l.data),
        });
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }

  salvar() {
    if (this.bloqueado()) return;
    if (this.form.invalid) {
      for (const c of Object.values(this.form.controls)) {
        c.markAsDirty();
        c.updateValueAndValidity({ onlySelf: true });
      }
      return;
    }
    const v = this.form.getRawValue();
    const body: LancamentoFinanceiroRequest = {
      tipo: v.tipo!,
      categoria: v.categoria!,
      descricao: v.descricao!.trim(),
      valor: v.valor!,
      data: paraLocalDateTime(v.data!),
    };
    const id = this.id();
    this.salvando.set(true);
    (id ? this.api.atualizar(id, body) : this.api.criar(body)).subscribe({
      next: () => {
        this.toast.sucesso(id ? 'Lançamento atualizado.' : 'Lançamento cadastrado.');
        this.router.navigate(['/financeiro']);
      },
      error: (e) => {
        this.salvando.set(false);
        this.toast.erro(mensagemDeErro(e));
      },
    });
  }
}
