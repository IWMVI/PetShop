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
import { FuncionarioApi, mensagemDeErro } from '../../../core/api';
import { Funcionario } from '../../../core/models';
import { ConfirmacaoService } from '../../../shared/confirmacao.service';
import { formatarCpf, formatarTelefone } from '../../../shared/format';
import { listagemPaginada } from '../../../shared/listagem/listagem-paginada';
import { Pagina } from '../../../shared/pagina/pagina';
import { CARGOS } from '../../../shared/rotulos/rotulos';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-funcionario-list',
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
  templateUrl: './funcionario-list.html',
  styleUrl: './funcionario-list.scss',
})
export class FuncionarioList implements OnInit {
  private readonly api = inject(FuncionarioApi);
  private readonly toast = inject(ToastService);
  private readonly confirmacao = inject(ConfirmacaoService);

  protected readonly lista = listagemPaginada((consulta) => this.api.listar(consulta));
  protected readonly cargos = CARGOS;
  protected readonly cpf = formatarCpf;
  protected readonly telefone = formatarTelefone;

  ngOnInit() {
    this.lista.recarregar();
  }

  confirmarExclusao(f: Funcionario) {
    this.confirmacao.perigo({
      titulo: `Excluir o funcionário ${f.nome}?`,
      conteudo: 'Os eventos que ele registrou no histórico dos pets continuam com o nome dele.',
      confirmar: 'Excluir',
      aoConfirmar: () => this.excluir(f),
    });
  }

  excluir(f: Funcionario) {
    this.api.excluir(f.id).subscribe({
      next: () => {
        this.toast.sucesso('Funcionário excluído.');
        this.lista.recarregar();
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }
}
