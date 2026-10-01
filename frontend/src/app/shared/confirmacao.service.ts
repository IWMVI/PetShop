import { Injectable, inject } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';

export interface Confirmacao {
  titulo: string;
  conteudo?: string;
  confirmar: string;
  cancelar?: string;
  aoConfirmar: () => void;
}

/** Diálogo de confirmação para ações destrutivas (excluir, cancelar). */
@Injectable({ providedIn: 'root' })
export class ConfirmacaoService {
  private readonly modal = inject(NzModalService);

  perigo(c: Confirmacao) {
    this.modal.confirm({
      nzTitle: c.titulo,
      nzContent: c.conteudo,
      nzOkText: c.confirmar,
      nzOkDanger: true,
      nzCancelText: c.cancelar ?? 'Cancelar',
      nzOnOk: () => c.aoConfirmar(),
    });
  }
}
