import { Component, DestroyRef, OnInit, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzTableModule } from 'ng-zorro-antd/table';
import { FinanceiroApi, TAMANHO_PAGINA, mensagemDeErro } from '../../../core/api';
import { LancamentoFinanceiro, StatusLancamento, TipoLancamento } from '../../../core/models';
import { ConfirmacaoService } from '../../../shared/confirmacao.service';
import { Estatistica } from '../../../shared/estatistica/estatistica';
import { formatarDataHora, formatarMoeda, paraLocalDateTime } from '../../../shared/format';
import { ItemTrilha, Pagina } from '../../../shared/pagina/pagina';
import { CATEGORIAS_LANCAMENTO, STATUS_LANCAMENTO } from '../../../shared/rotulos/rotulos';
import { SaldoFinanceiroService } from '../../../shared/saldo-financeiro.service';
import { Status, TomStatus } from '../../../shared/status/status';
import { ToastService } from '../../../shared/toast/toast.service';

/** Opções do filtro de status da listagem; "TODAS" não manda o parâmetro status para a API. */
type FiltroStatus = StatusLancamento | 'TODAS';

const OPCOES_FILTRO: { valor: FiltroStatus; rotulo: string }[] = [
  { valor: 'PENDENTE', rotulo: 'Pendentes' },
  { valor: 'PAGO', rotulo: 'Pagas' },
  { valor: 'CANCELADO', rotulo: 'Canceladas' },
  { valor: 'TODAS', rotulo: 'Todas' },
];

/**
 * Listagem de contas a pagar ou a receber (mesma tela para os dois, conforme `tipo`).
 * Reaproveita a entidade/endpoints de lançamento financeiro: uma conta é um lançamento
 * com vencimento e sem data de pagamento, que fica PENDENTE até ser paga ou cancelada.
 */
@Component({
  selector: 'app-contas',
  imports: [
    Pagina,
    ReactiveFormsModule,
    RouterLink,
    LucideAngularModule,
    Estatistica,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzDropDownModule,
    NzEmptyModule,
    NzGridModule,
    NzSelectModule,
    NzTableModule,
    Status,
  ],
  templateUrl: './contas.html',
  styleUrl: './contas.scss',
})
export class Contas implements OnInit {
  readonly tipo = input.required<TipoLancamento>();

  private readonly api = inject(FinanceiroApi);
  private readonly toast = inject(ToastService);
  private readonly confirmacao = inject(ConfirmacaoService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly saldoFinanceiro = inject(SaldoFinanceiroService);

  protected readonly titulo = computed(() =>
    this.tipo() === 'SAIDA' ? 'Contas a Pagar' : 'Contas a Receber',
  );
  protected readonly trilha = computed<ItemTrilha[]>(() => [
    { rotulo: 'Financeiro', link: '/financeiro/extrato' },
    { rotulo: this.titulo() },
  ]);
  protected readonly rotaNova = computed(() =>
    this.tipo() === 'SAIDA'
      ? '/financeiro/contas-a-pagar/novo'
      : '/financeiro/contas-a-receber/novo',
  );
  protected readonly rotuloNova = computed(() =>
    this.tipo() === 'SAIDA' ? 'Nova conta a pagar' : 'Nova conta a receber',
  );
  protected readonly rotuloMarcar = computed(() =>
    this.tipo() === 'SAIDA' ? 'Marcar como paga' : 'Marcar como recebida',
  );
  /** Resumo de saldo desta tela: reaproveita o mesmo serviço do badge do menu lateral. */
  protected readonly saldo = computed(() =>
    this.tipo() === 'SAIDA' ? this.saldoFinanceiro.aPagar() : this.saldoFinanceiro.aReceber(),
  );

  protected readonly filtroStatus =
    inject(FormBuilder).nonNullable.control<FiltroStatus>('PENDENTE');

  protected readonly itens = signal<LancamentoFinanceiro[]>([]);
  protected readonly total = signal(0);
  protected readonly pagina = signal(0);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  protected readonly tamanho = TAMANHO_PAGINA;
  protected readonly opcoesFiltro = OPCOES_FILTRO;
  protected readonly categoriasLancamento = CATEGORIAS_LANCAMENTO;
  protected readonly moeda = formatarMoeda;
  protected readonly dataHora = formatarDataHora;

  ngOnInit() {
    this.carregar();
    this.filtroStatus.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.pagina.set(0);
      this.carregar();
    });
  }

  irPara(indice: number) {
    this.pagina.set(indice);
    this.carregar();
  }

  rotaEditar(l: LancamentoFinanceiro): (string | number)[] {
    const base =
      this.tipo() === 'SAIDA' ? '/financeiro/contas-a-pagar' : '/financeiro/contas-a-receber';
    return [base, l.id, 'editar'];
  }

  /** Rótulo e tom do status exibidos no badge: uma conta vencida se destaca em vez de "Pendente". */
  statusExibido(l: LancamentoFinanceiro): { rotulo: string; tom: TomStatus } {
    if (l.status === 'PENDENTE' && l.vencido) {
      return { rotulo: 'Vencida', tom: 'erro' };
    }
    return STATUS_LANCAMENTO[l.status];
  }

  confirmarPagamento(l: LancamentoFinanceiro) {
    this.confirmacao.perigo({
      titulo: `${this.rotuloMarcar()} esta conta?`,
      conteudo: `${this.categoriasLancamento[l.categoria]} · ${this.moeda(l.valor)}`,
      confirmar: this.rotuloMarcar(),
      cancelar: 'Voltar',
      aoConfirmar: () => this.marcarComoPaga(l),
    });
  }

  marcarComoPaga(l: LancamentoFinanceiro) {
    this.api.marcarComoPaga(l.id, { dataPagamento: paraLocalDateTime(new Date()) }).subscribe({
      next: () => {
        this.toast.sucesso(
          this.tipo() === 'SAIDA' ? 'Conta marcada como paga.' : 'Conta marcada como recebida.',
        );
        this.carregar();
        this.saldoFinanceiro.carregar();
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }

  confirmarCancelamento(l: LancamentoFinanceiro) {
    this.confirmacao.perigo({
      titulo: 'Cancelar esta conta?',
      conteudo: `${this.categoriasLancamento[l.categoria]} · ${this.moeda(l.valor)}`,
      confirmar: 'Cancelar conta',
      cancelar: 'Voltar',
      aoConfirmar: () => this.cancelar(l),
    });
  }

  cancelar(l: LancamentoFinanceiro) {
    this.api.cancelar(l.id).subscribe({
      next: () => {
        this.toast.sucesso('Conta cancelada.');
        this.carregar();
        this.saldoFinanceiro.carregar();
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }

  private carregar() {
    this.carregando.set(true);
    const filtro = this.filtroStatus.value;
    const status = filtro === 'TODAS' ? undefined : filtro;
    this.api.listarContas(this.tipo(), status, this.pagina(), this.tamanho).subscribe({
      next: (r) => {
        this.itens.set(r.itens);
        this.total.set(r.total);
        this.erro.set(null);
        this.carregando.set(false);
      },
      error: (e) => {
        this.erro.set(mensagemDeErro(e));
        this.carregando.set(false);
      },
    });
  }
}
