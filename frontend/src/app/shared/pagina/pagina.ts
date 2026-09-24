import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';

/** Item da trilha de navegação (breadcrumb). Sem `link`, é exibido como texto (página atual). */
export interface ItemTrilha {
  rotulo: string;
  link?: string | readonly (string | number)[];
}

/**
 * Estrutura padrão de uma tela: trilha de navegação, título, ações e conteúdo.
 * Toda tela nova deve usá-la para manter o mesmo cabeçalho e espaçamentos.
 *
 * @example
 * <app-pagina titulo="Tutores" [trilha]="[{ rotulo: 'Tutores' }]">
 *   <a paginaAcoes nz-button nzType="primary" class="btn-novo" routerLink="novo">Novo tutor</a>
 *   <span paginaResumo>Texto opcional abaixo do título</span>
 *   ...conteúdo da tela...
 * </app-pagina>
 */
@Component({
  selector: 'app-pagina',
  imports: [RouterLink, NzBreadCrumbModule],
  templateUrl: './pagina.html',
  styleUrl: './pagina.scss',
})
export class Pagina {
  readonly titulo = input.required<string>();
  readonly trilha = input<ItemTrilha[]>([]);
}
