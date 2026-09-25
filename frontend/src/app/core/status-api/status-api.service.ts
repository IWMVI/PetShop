import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, map, of, switchMap, timer } from 'rxjs';
import { API_URL } from '../api';

/** online: API e banco ok · instavel: API responde, mas algum componente (ex.: banco) falhou. */
export type SituacaoApi = 'verificando' | 'online' | 'instavel' | 'offline';

/** Intervalo entre verificações da saúde da API. */
export const INTERVALO_STATUS_MS = 30_000;

/**
 * Consulta periodicamente o /actuator/health do back-end (Spring Boot Actuator)
 * e expõe a situação da API para o indicador de status do menu.
 */
@Injectable({ providedIn: 'root' })
export class StatusApiService {
  private readonly http = inject(HttpClient);
  private readonly _situacao = signal<SituacaoApi>('verificando');
  readonly situacao = this._situacao.asReadonly();

  constructor() {
    timer(0, INTERVALO_STATUS_MS)
      .pipe(
        switchMap(() =>
          this.http.get<{ status: string }>(`${API_URL}/actuator/health`).pipe(
            map((r): SituacaoApi => (r.status === 'UP' ? 'online' : 'instavel')),
            // 503 com corpo do Actuator = API no ar com algum componente fora (ex.: banco).
            catchError((e: HttpErrorResponse) =>
              of<SituacaoApi>(e.status === 503 && e.error?.status ? 'instavel' : 'offline'),
            ),
          ),
        ),
        takeUntilDestroyed(inject(DestroyRef)),
      )
      .subscribe((s) => this._situacao.set(s));
  }
}
