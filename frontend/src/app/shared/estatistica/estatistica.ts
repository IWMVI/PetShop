import { Component, input } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';

/** Tom visual de uma estatística: neutro, sucesso (verde) ou erro (vermelho). */
export type TomEstatistica = 'neutro' | 'sucesso' | 'erro';

/**
 * Item de resumo com ícone, rótulo e valor em destaque, usado nos cards de totais
 * do módulo Financeiro (Extrato, Contas a Pagar/Receber). Mantém a mesma aparência
 * em todas as telas que mostram esse tipo de resumo.
 *
 * @example <app-estatistica icone="wallet" rotulo="Saldo" [valor]="moeda(saldo)" />
 */
@Component({
  selector: 'app-estatistica',
  imports: [LucideAngularModule],
  templateUrl: './estatistica.html',
  styleUrl: './estatistica.scss',
  host: {
    '[class]': "'tom-' + tom()",
  },
})
export class Estatistica {
  readonly icone = input.required<string>();
  readonly rotulo = input.required<string>();
  readonly valor = input.required<string>();
  readonly tom = input<TomEstatistica>('neutro');
}
