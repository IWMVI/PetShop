import { TestBed } from '@angular/core/testing';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  const message = { success: jest.fn(), error: jest.fn() };

  beforeEach(() => {
    jest.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [{ provide: NzMessageService, useValue: message }],
    });
  });

  it('exibe mensagens de sucesso', () => {
    TestBed.inject(ToastService).sucesso('ok');
    expect(message.success).toHaveBeenCalledWith('ok');
  });

  it('exibe erros por mais tempo', () => {
    TestBed.inject(ToastService).erro('falhou');
    expect(message.error).toHaveBeenCalledWith('falhou', { nzDuration: 6000 });
  });
});
