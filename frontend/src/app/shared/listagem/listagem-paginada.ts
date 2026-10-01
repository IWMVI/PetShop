import { DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  EMPTY,
  Observable,
  Subject,
  catchError,
  debounceTime,
  distinctUntilChanged,
  merge,
  switchMap,
  tap,
} from 'rxjs';
import { TAMANHO_PAGINA, mensagemDeErro } from '../../core/api';
import { ConsultaPaginada, Pagina } from '../../core/models';

/** Espera após a última tecla antes de buscar no servidor. */
export const ATRASO_BUSCA_MS = 300;

/**
 * Estado de uma tabela paginada no servidor: carrega somente a página exibida,
 * refaz a consulta ao trocar de página e aplica a busca com debounce.
 * Deve ser criada em contexto de injeção (campo do componente); a primeira carga
 * acontece ao chamar `recarregar()` (normalmente no ngOnInit, quando os inputs já existem).
 *
 * @example
 * protected readonly lista = listagemPaginada((c) => this.api.listar(c));
 * // template: [nzData]="lista.itens()" [nzTotal]="lista.total()" (nzPageIndexChange)="lista.irPara($event - 1)"
 */
export function listagemPaginada<T>(
  carregar: (consulta: ConsultaPaginada) => Observable<Pagina<T>>,
) {
  const destroyRef = inject(DestroyRef);

  const itens = signal<T[]>([]);
  const total = signal(0);
  const pagina = signal(0);
  const carregando = signal(true);
  const erro = signal<string | null>(null);
  const termo = signal('');

  const recarregar$ = new Subject<void>();
  const busca$ = new Subject<string>();

  merge(
    recarregar$,
    busca$.pipe(
      debounceTime(ATRASO_BUSCA_MS),
      distinctUntilChanged(),
      tap((t) => {
        termo.set(t);
        pagina.set(0);
      }),
    ),
  )
    .pipe(
      tap(() => carregando.set(true)),
      switchMap(() =>
        carregar({ pagina: pagina(), tamanho: TAMANHO_PAGINA, busca: termo() }).pipe(
          tap((p) => {
            // Página esvaziada (ex.: último item excluído): volta para a anterior.
            if (p.itens.length === 0 && p.pagina > 0) {
              pagina.set(p.pagina - 1);
              recarregar$.next();
              return;
            }
            itens.set(p.itens);
            total.set(p.total);
            erro.set(null);
            carregando.set(false);
          }),
          // Mantém a listagem viva após uma falha: a próxima troca de página tenta de novo.
          catchError((e) => {
            erro.set(mensagemDeErro(e));
            carregando.set(false);
            return EMPTY;
          }),
        ),
      ),
      takeUntilDestroyed(destroyRef),
    )
    .subscribe();

  return {
    itens: itens.asReadonly(),
    total: total.asReadonly(),
    /** Índice da página atual, começando em 0. */
    pagina: pagina.asReadonly(),
    carregando: carregando.asReadonly(),
    erro: erro.asReadonly(),
    termo: termo.asReadonly(),
    tamanho: TAMANHO_PAGINA,
    recarregar: () => recarregar$.next(),
    irPara: (indice: number) => {
      pagina.set(indice);
      recarregar$.next();
    },
    buscar: (t: string) => busca$.next(t),
  };
}

export type ListagemPaginada<T> = ReturnType<typeof listagemPaginada<T>>;
