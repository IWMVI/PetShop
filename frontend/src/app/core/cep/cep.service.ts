import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';

/** Endereço retornado pela consulta de CEP. */
export interface EnderecoCep {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  cidade: string;
  estado: string;
}

interface ViaCepResposta {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  localidade: string;
  uf: string;
  erro?: boolean | string;
}

export const VIACEP_URL = 'https://viacep.com.br/ws';

/** Consulta de CEP pela API pública e gratuita do ViaCEP (https://viacep.com.br). */
@Injectable({ providedIn: 'root' })
export class CepService {
  private readonly http = inject(HttpClient);

  /** Emite o endereço, ou null se o CEP não existir ou a consulta falhar. */
  buscar(cep: string): Observable<EnderecoCep | null> {
    const digitos = cep.replace(/\D/g, '');
    if (digitos.length !== 8) return of(null);

    return this.http.get<ViaCepResposta>(`${VIACEP_URL}/${digitos}/json/`).pipe(
      map((r) =>
        r.erro
          ? null
          : {
              cep: r.cep,
              logradouro: r.logradouro,
              complemento: r.complemento,
              bairro: r.bairro,
              cidade: r.localidade,
              estado: r.uf,
            },
      ),
      catchError(() => of(null)),
    );
  }
}
