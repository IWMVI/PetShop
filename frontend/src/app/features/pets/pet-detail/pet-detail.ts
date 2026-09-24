import { Component, OnInit, computed, inject, input, numberAttribute, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { AgendamentoApi, PetApi, mensagemDeErro } from '../../../core/api';
import { Agendamento, AgendamentoStatus, Pet } from '../../../core/models';
import { formatarDataHora, formatarMoeda, formatarPeso } from '../../../shared/format';
import { ConfirmacaoService } from '../../../shared/confirmacao.service';
import { listagemPaginada } from '../../../shared/listagem/listagem-paginada';
import { ToastService } from '../../../shared/toast/toast.service';
import { ItemTrilha, Pagina } from '../../../shared/pagina/pagina';

const STATUS: Record<AgendamentoStatus, { rotulo: string; cor: string }> = {
  AGENDADO: { rotulo: 'Agendado', cor: 'gold' },
  CONCLUIDO: { rotulo: 'Concluído', cor: 'green' },
  CANCELADO: { rotulo: 'Cancelado', cor: 'default' },
};

@Component({
  selector: 'app-pet-detail',
  imports: [
    Pagina,
    RouterLink,
    LucideAngularModule,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzDropDownModule,
    NzEmptyModule,
    NzSkeletonModule,
    NzTableModule,
    NzTagModule,
  ],
  templateUrl: './pet-detail.html',
  styleUrl: './pet-detail.scss',
})
export class PetDetail implements OnInit {
  readonly tutorId = input.required({ transform: numberAttribute });
  readonly petId = input.required({ transform: numberAttribute });

  private readonly petApi = inject(PetApi);
  private readonly agendamentoApi = inject(AgendamentoApi);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly confirmacao = inject(ConfirmacaoService);

  protected readonly pet = signal<Pet | null>(null);
  /** Agendamentos paginados no servidor (ordenados por data e hora). */
  protected readonly agendamentos = listagemPaginada((consulta) =>
    this.agendamentoApi.listar(this.petId(), consulta),
  );
  protected readonly erro = signal<string | null>(null);
  protected readonly moeda = formatarMoeda;
  protected readonly kg = formatarPeso;
  protected readonly dataHora = formatarDataHora;
  protected readonly status = STATUS;
  protected readonly trilha = computed<ItemTrilha[]>(() => [
    { rotulo: 'Tutores', link: '/tutores' },
    { rotulo: 'Tutor', link: ['/tutores', this.tutorId()] },
    { rotulo: this.pet()?.nome ?? '…' },
  ]);

  ngOnInit() {
    this.petApi.buscar(this.tutorId(), this.petId()).subscribe({
      next: (p) => this.pet.set(p),
      error: (e) => this.erro.set(mensagemDeErro(e)),
    });
    this.agendamentos.recarregar();
  }

  nomesServicos(a: Agendamento) {
    return a.servicos.map((s) => s.nome ?? `#${s.servicoId}`).join(', ');
  }

  confirmarCancelamento(a: Agendamento) {
    this.confirmacao.perigo({
      titulo: 'Cancelar este agendamento?',
      conteudo: `${this.dataHora(a.dataHora)} — ${this.nomesServicos(a)}`,
      confirmar: 'Cancelar agendamento',
      cancelar: 'Voltar',
      aoConfirmar: () => this.cancelar(a),
    });
  }

  cancelar(a: Agendamento) {
    this.agendamentoApi.cancelar(this.petId(), a.id).subscribe({
      next: () => {
        this.toast.sucesso('Agendamento cancelado.');
        this.agendamentos.recarregar();
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }

  confirmarExclusao(p: Pet) {
    this.confirmacao.perigo({
      titulo: `Excluir o pet ${p.nome}?`,
      conteudo: 'Os agendamentos do pet deixam de aparecer.',
      confirmar: 'Excluir',
      aoConfirmar: () => this.excluir(p),
    });
  }

  excluir(p: Pet) {
    this.petApi.excluir(this.tutorId(), p.id).subscribe({
      next: () => {
        this.toast.sucesso('Pet excluído.');
        this.router.navigate(['/tutores', this.tutorId()]);
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }
}
