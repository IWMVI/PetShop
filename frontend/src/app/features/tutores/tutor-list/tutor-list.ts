import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzTableModule } from 'ng-zorro-antd/table';
import { TutorApi, mensagemDeErro } from '../../../core/api';
import { Tutor } from '../../../core/models';
import { formatarCpf, formatarTelefone } from '../../../shared/format';
import { ConfirmacaoService } from '../../../shared/confirmacao.service';
import { listagemPaginada } from '../../../shared/listagem/listagem-paginada';
import { ToastService } from '../../../shared/toast/toast.service';
import { Pagina } from '../../../shared/pagina/pagina';

@Component({
  selector: 'app-tutor-list',
  imports: [
    Pagina,
    RouterLink,
    LucideAngularModule,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzDropDownModule,
    NzEmptyModule,
    NzInputModule,
    NzTableModule,
  ],
  templateUrl: './tutor-list.html',
  styleUrl: './tutor-list.scss',
})
export class TutorList implements OnInit {
  private readonly api = inject(TutorApi);
  private readonly toast = inject(ToastService);
  private readonly confirmacao = inject(ConfirmacaoService);

  /** Somente a página exibida é carregada; a busca é feita no servidor. */
  protected readonly lista = listagemPaginada((consulta) => this.api.listar(consulta));
  protected readonly telefone = formatarTelefone;
  protected readonly cpf = formatarCpf;

  ngOnInit() {
    this.lista.recarregar();
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
    this.api.excluir(t.id).subscribe({
      next: () => {
        this.toast.sucesso('Tutor excluído.');
        this.lista.recarregar();
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }
}
