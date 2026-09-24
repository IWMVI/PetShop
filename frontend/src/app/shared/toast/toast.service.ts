import { Injectable, inject } from '@angular/core';
import { NzMessageService } from 'ng-zorro-antd/message';

/** Fachada sobre o NzMessageService para padronizar os avisos da aplicação. */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly message = inject(NzMessageService);

  sucesso(texto: string) {
    this.message.success(texto);
  }

  erro(texto: string) {
    this.message.error(texto, { nzDuration: 6000 });
  }
}
