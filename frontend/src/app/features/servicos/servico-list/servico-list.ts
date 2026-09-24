import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzTableModule } from 'ng-zorro-antd/table';
import { ServicoApi, mensagemDeErro } from '../../../core/api';
import { Servico } from '../../../core/models';
import { formatarMoeda } from '../../../shared/format';
import { ConfirmacaoService } from '../../../shared/confirmacao.service';
import { listagemPaginada } from '../../../shared/listagem/listagem-paginada';
import { ToastService } from '../../../shared/toast/toast.service';
import { Pagina } from '../../../shared/pagina/pagina';

@Component({
  selector: 'app-servico-list',
  imports: [
    Pagina,
    RouterLink,
    LucideAngularModule,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzDropDownModule,
    NzEmptyModule,
    NzTableModule,
  ],
  templateUrl: './servico-list.html',
  styleUrl: './servico-list.scss',
})
export class ServicoList implements OnInit {
  private readonly api = inject(ServicoApi);
  private readonly toast = inject(ToastService);
  private readonly confirmacao = inject(ConfirmacaoService);

  protected readonly lista = listagemPaginada((consulta) => this.api.listar(consulta));
  protected readonly moeda = formatarMoeda;

  ngOnInit() {
    this.lista.recarregar();
  }

  confirmarExclusao(s: Servico) {
    this.confirmacao.perigo({
      titulo: `Excluir o serviço ${s.nome}?`,
      conteudo: 'Agendamentos já feitos mantêm o preço cobrado.',
      confirmar: 'Excluir',
      aoConfirmar: () => this.excluir(s),
    });
  }

  excluir(s: Servico) {
    this.api.excluir(s.id).subscribe({
      next: () => {
        this.toast.sucesso('Serviço excluído.');
        this.lista.recarregar();
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }
}
