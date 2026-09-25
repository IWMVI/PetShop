import { Component, booleanAttribute, computed, input } from '@angular/core';

/**
 * Tom visual de um status, na paleta do sistema:
 * destaque (amarelo), sucesso (verde), alerta (laranja), erro (vermelho) e neutro (cinza).
 */
export type TomStatus = 'destaque' | 'sucesso' | 'alerta' | 'erro' | 'neutro';

/**
 * Badge de status: ponto colorido e rótulo. Use sempre este componente para exibir
 * status na aplicação, para manter cores e formato iguais em todas as telas.
 *
 * @example <app-status tom="sucesso">Concluído</app-status>
 */
@Component({
  selector: 'app-status',
  templateUrl: './status.html',
  styleUrl: './status.scss',
  host: {
    role: 'status',
    '[class]': 'classe()',
    '[class.compacto]': 'compacto()',
    '[attr.aria-label]': 'compacto() ? rotulo() : null',
    '[attr.title]': 'compacto() ? rotulo() : null',
  },
})
export class Status {
  readonly tom = input<TomStatus>('neutro');
  /** Mostra só o ponto (o texto vira dica e rótulo acessível). */
  readonly compacto = input(false, { transform: booleanAttribute });
  /** Texto usado no modo compacto; no modo normal o conteúdo do componente é exibido. */
  readonly rotulo = input('');
  /** Ponto pulsante, para estados em andamento (ex.: verificando). */
  readonly pulsando = input(false, { transform: booleanAttribute });

  protected readonly classe = computed(() => `status status-${this.tom()}`);
}
