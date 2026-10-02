import { Component, OnInit, computed, inject, input, numberAttribute, signal } from '@angular/core';
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
import { CategoriaLancamento, ContaFinanceiraRequest, TipoLancamento } from '../../../core/models';
import { paraLocalDateTime } from '../../../shared/format';
import { MoedaDirective } from '../../../shared/moeda/moeda.directive';
import { ItemTrilha, Pagina } from '../../../shared/pagina/pagina';
import {
  CATEGORIAS_ENTRADA,
  CATEGORIAS_LANCAMENTO,
  CATEGORIAS_SAIDA,
} from '../../../shared/rotulos/rotulos';
import { SaldoFinanceiroService } from '../../../shared/saldo-financeiro.service';
import { ToastService } from '../../../shared/toast/toast.service';

/**
 * Cadastro/edição de conta a pagar ou a receber (`tipo` fixo, vindo da rota via
 * `withComponentInputBinding`, ao contrário do LancamentoForm onde o tipo é escolhido
 * no próprio formulário). Reaproveita o GET de lançamento por id para carregar a conta
 * na edição, já que é a mesma entidade.
 */
@Component({
  selector: 'app-conta-form',
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
  templateUrl: './conta-form.html',
  styleUrl: './conta-form.scss',
})
export class ContaForm implements OnInit {
  readonly tipo = input.required<TipoLancamento>();
  readonly id = input(undefined, { transform: numberAttribute });

  private readonly api = inject(FinanceiroApi);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly saldoFinanceiro = inject(SaldoFinanceiroService);

  protected readonly salvando = signal(false);
  /** Defesa: uma conta já paga/cancelada não deveria chegar a esta tela editável. */
  protected readonly bloqueado = signal(false);

  protected readonly categoriasLancamento = CATEGORIAS_LANCAMENTO;
  protected readonly categorias = computed(() => {
    const lista = this.tipo() === 'SAIDA' ? CATEGORIAS_SAIDA : CATEGORIAS_ENTRADA;
    return lista.map((valor) => ({ valor, rotulo: CATEGORIAS_LANCAMENTO[valor] }));
  });

  protected readonly rotaLista = computed(() =>
    this.tipo() === 'SAIDA' ? '/financeiro/contas-a-pagar' : '/financeiro/contas-a-receber',
  );
  private readonly tituloLista = computed(() =>
    this.tipo() === 'SAIDA' ? 'Contas a Pagar' : 'Contas a Receber',
  );
  protected readonly titulo = computed(() => {
    const nome = this.tipo() === 'SAIDA' ? 'conta a pagar' : 'conta a receber';
    return this.id() ? `Editar ${nome}` : `Nova ${nome}`;
  });
  protected readonly trilha = computed<ItemTrilha[]>(() => [
    { rotulo: 'Financeiro', link: '/financeiro/dashboard' },
    { rotulo: this.tituloLista(), link: this.rotaLista() },
    { rotulo: this.id() ? 'Editar' : 'Nova' },
  ]);

  protected readonly form = inject(FormBuilder).group({
    categoria: [null as CategoriaLancamento | null, Validators.required],
    descricao: ['', [Validators.required, Validators.maxLength(255)]],
    valor: [null as number | null, [Validators.required, Validators.min(0.01)]],
    dataVencimento: [null as Date | null, Validators.required],
  });

  ngOnInit() {
    const id = this.id();
    if (!id) return;
    this.api.buscar(id).subscribe({
      next: (c) => {
        if (c.status !== 'PENDENTE' || c.pagamentoId != null) {
          this.bloqueado.set(true);
          this.form.disable();
        }
        this.form.patchValue({
          categoria: c.categoria,
          descricao: c.descricao,
          valor: c.valor,
          dataVencimento: c.dataVencimento ? new Date(c.dataVencimento) : null,
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
    const body: ContaFinanceiraRequest = {
      tipo: this.tipo(),
      categoria: v.categoria!,
      descricao: v.descricao!.trim(),
      valor: v.valor!,
      dataVencimento: paraLocalDateTime(v.dataVencimento!),
    };
    const id = this.id();
    this.salvando.set(true);
    (id ? this.api.atualizarConta(id, body) : this.api.registrarConta(body)).subscribe({
      next: () => {
        this.toast.sucesso(id ? 'Conta atualizada.' : 'Conta cadastrada.');
        this.saldoFinanceiro.carregar();
        this.router.navigate([this.rotaLista()]);
      },
      error: (e) => {
        this.salvando.set(false);
        this.toast.erro(mensagemDeErro(e));
      },
    });
  }
}
