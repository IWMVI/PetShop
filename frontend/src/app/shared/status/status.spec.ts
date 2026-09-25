import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Status } from './status';

@Component({
  imports: [Status],
  template: `
    <app-status tom="sucesso">Concluído</app-status>
    <app-status tom="erro" compacto rotulo="API offline" />
  `,
})
class Hospedeiro {}

describe('Status', () => {
  const montar = () => {
    const fixture = TestBed.createComponent(Hospedeiro);
    fixture.detectChanges();
    return Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('app-status'));
  };

  it('exibe ponto e texto com a classe do tom', () => {
    const [normal] = montar();
    expect(normal.classList).toContain('status-sucesso');
    expect(normal.querySelector('.ponto')).not.toBeNull();
    expect(normal.textContent?.trim()).toBe('Concluído');
    expect(normal.getAttribute('role')).toBe('status');
  });

  it('no modo compacto mostra só o ponto, com rótulo acessível', () => {
    const [, compacto] = montar();
    expect(compacto.classList).toContain('compacto');
    expect(compacto.querySelector('.texto')).toBeNull();
    expect(compacto.getAttribute('aria-label')).toBe('API offline');
    expect(compacto.getAttribute('title')).toBe('API offline');
  });
});
