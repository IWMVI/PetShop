import { Component, OnInit, computed, inject, input, numberAttribute, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { NzTableModule } from 'ng-zorro-antd/table';
import { AgendamentoApi, PagamentoApi, mensagemDeErro } from '../../../core/api';
import { Agendamento, AgendamentoStatus, Pagamento } from '../../../core/models';
import { ConfirmacaoService } from '../../../shared/confirmacao.service';
import { formatarDataHora, formatarMoeda, paraLocalDateTime } from '../../../shared/format';
import { ItemTrilha, Pagina } from '../../../shared/pagina/pagina';
import { METODOS_PAGAMENTO, STATUS_PAGAMENTO } from '../../../shared/rotulos/rotulos';
import { Status, TomStatus } from '../../../shared/status/status';
import { ToastService } from '../../../shared/toast/toast.service';

const STATUS_AGENDAMENTO: Record<AgendamentoStatus, { rotulo: string; tom: TomStatus }> = {
  AGENDADO: { rotulo: 'Agendado', tom: 'destaque' },
  CONCLUIDO: { rotulo: 'Concluído', tom: 'sucesso' },
  CANCELADO: { rotulo: 'Cancelado', tom: 'neutro' },
};

@Component({
  selector: 'app-agendamento-detail',
  imports: [
    Pagina,
    RouterLink,
    LucideAngularModule,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzEmptyModule,
    NzSkeletonModule,
    NzTableModule,
    Status,
  ],
  templateUrl: './agendamento-detail.html',
  styleUrl: './agendamento-detail.scss',
})
export class AgendamentoDetail implements OnInit {
  readonly tutorId = input.required({ transform: numberAttribute });
  readonly petId = input.required({ transform: numberAttribute });
  readonly agendamentoId = input.required({ transform: numberAttribute });

  private readonly agendamentoApi = inject(AgendamentoApi);
  private readonly pagamentoApi = inject(PagamentoApi);
  private readonly toast = inject(ToastService);
  private readonly confirmacao = inject(ConfirmacaoService);

  protected readonly agendamento = signal<Agendamento | null>(null);
  protected readonly erro = signal<string | null>(null);
  /** Pagamentos do agendamento: a API devolve a lista completa, sem paginação. */
  protected readonly pagamentos = signal<Pagamento[]>([]);
  protected readonly carregandoPagamentos = signal(true);
  protected readonly erroPagamentos = signal<string | null>(null);
  /** Id do pagamento com uma ação em andamento, para mostrar loading só nele. */
  protected readonly atualizandoId = signal<number | null>(null);

  protected readonly statusAgendamento = STATUS_AGENDAMENTO;
  protected readonly statusPagamento = STATUS_PAGAMENTO;
  protected readonly metodosPagamento = METODOS_PAGAMENTO;
  protected readonly moeda = formatarMoeda;
  protected readonly dataHora = formatarDataHora;

  protected readonly trilha = computed<ItemTrilha[]>(() => [
    { rotulo: 'Tutores', link: '/tutores' },
    { rotulo: 'Tutor', link: ['/tutores', this.tutorId()] },
    { rotulo: 'Pet', link: ['/tutores', this.tutorId(), 'pets', this.petId()] },
    { rotulo: 'Agendamento' },
  ]);

  nomesServicos(a: Agendamento) {
    return a.servicos.map((s) => s.nome ?? `#${s.servicoId}`).join(', ');
  }

  ngOnInit() {
    this.agendamentoApi.buscar(this.petId(), this.agendamentoId()).subscribe({
      next: (a) => this.agendamento.set(a),
      error: (e) => this.erro.set(mensagemDeErro(e)),
    });
    this.carregarPagamentos();
  }

  carregarPagamentos() {
    this.carregandoPagamentos.set(true);
    this.pagamentoApi.listar(this.agendamentoId()).subscribe({
      next: (pagamentos) => {
        this.pagamentos.set(pagamentos);
        this.carregandoPagamentos.set(false);
      },
      error: (e) => {
        this.erroPagamentos.set(mensagemDeErro(e));
        this.carregandoPagamentos.set(false);
      },
    });
  }

  marcarComoPago(p: Pagamento) {
    this.atualizandoId.set(p.id);
    this.pagamentoApi
      .atualizarStatus(p.id, { status: 'PAGO', dataPagamento: paraLocalDateTime(new Date()) })
      .subscribe({
        next: () => {
          this.toast.sucesso('Pagamento marcado como pago.');
          this.atualizandoId.set(null);
          this.carregarPagamentos();
        },
        error: (e) => {
          this.atualizandoId.set(null);
          this.toast.erro(mensagemDeErro(e));
        },
      });
  }

  confirmarCancelamento(p: Pagamento) {
    this.confirmacao.perigo({
      titulo: 'Cancelar este pagamento?',
      conteudo: `${this.moeda(p.valor)} · ${this.metodosPagamento[p.metodoPagamento]}`,
      confirmar: 'Cancelar pagamento',
      cancelar: 'Voltar',
      aoConfirmar: () => this.cancelar(p),
    });
  }

  cancelar(p: Pagamento) {
    this.pagamentoApi.cancelar(p.id).subscribe({
      next: () => {
        this.toast.sucesso('Pagamento cancelado.');
        this.carregarPagamentos();
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }
}
