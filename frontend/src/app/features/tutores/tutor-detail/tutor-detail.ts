import { Component, computed, OnInit, inject, input, numberAttribute, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { PetApi, TutorApi, mensagemDeErro } from '../../../core/api';
import { Pet, Tutor } from '../../../core/models';
import { formatarCpf, formatarPeso, formatarTelefone } from '../../../shared/format';
import { ConfirmacaoService } from '../../../shared/confirmacao.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { ItemTrilha, Pagina } from '../../../shared/pagina/pagina';

@Component({
  selector: 'app-tutor-detail',
  imports: [
    Pagina,
    RouterLink,
    LucideAngularModule,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzDescriptionsModule,
    NzEmptyModule,
    NzGridModule,
    NzSkeletonModule,
  ],
  templateUrl: './tutor-detail.html',
  styleUrl: './tutor-detail.scss',
})
export class TutorDetail implements OnInit {
  readonly id = input.required({ transform: numberAttribute });

  private readonly tutorApi = inject(TutorApi);
  private readonly petApi = inject(PetApi);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly confirmacao = inject(ConfirmacaoService);

  protected readonly tutor = signal<Tutor | null>(null);
  protected readonly pets = signal<Pet[]>([]);
  protected readonly erro = signal<string | null>(null);
  protected readonly telefone = formatarTelefone;
  protected readonly cpf = formatarCpf;
  protected readonly trilha = computed<ItemTrilha[]>(() => [
    { rotulo: 'Tutores', link: '/tutores' },
    { rotulo: this.tutor()?.nome ?? '…' },
  ]);
  protected readonly kg = formatarPeso;

  ngOnInit() {
    this.tutorApi.buscar(this.id()).subscribe({
      next: (t) => this.tutor.set(t),
      error: (e) => this.erro.set(mensagemDeErro(e)),
    });
    this.petApi.listar(this.id()).subscribe({
      next: (ps) => this.pets.set(ps),
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }

  confirmarExclusao(t: Tutor) {
    this.confirmacao.perigo({
      titulo: `Excluir o tutor ${t.nome}?`,
      conteudo:
        'O tutor deixa de aparecer nas listagens. Ele pode ser recuperado pelo CPF em até 30 dias; depois disso, ele e seus pets e agendamentos são apagados definitivamente.',
      confirmar: 'Excluir',
      aoConfirmar: () => this.excluir(t),
    });
  }

  excluir(t: Tutor) {
    this.tutorApi.excluir(t.id).subscribe({
      next: () => {
        this.toast.sucesso('Tutor excluído.');
        this.router.navigate(['/tutores']);
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }
}
