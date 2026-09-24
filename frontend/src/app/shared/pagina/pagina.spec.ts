import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideTestEnv } from '../../../testing/providers';
import { ItemTrilha, Pagina } from './pagina';

@Component({
  imports: [Pagina],
  template: `
    <app-pagina titulo="Rex" [trilha]="trilha">
      <span paginaResumo>Cachorro · Labrador</span>
      <button paginaAcoes type="button">Editar</button>
      <p class="conteudo">Agendamentos</p>
    </app-pagina>
  `,
})
class Hospedeiro {
  trilha: ItemTrilha[] = [
    { rotulo: 'Tutores', link: '/tutores' },
    { rotulo: 'Ana', link: ['/tutores', 1] },
    { rotulo: 'Rex' },
  ];
}

describe('Pagina', () => {
  const montar = (trilha?: ItemTrilha[]) => {
    TestBed.configureTestingModule({ imports: [Hospedeiro], providers: provideTestEnv() });
    const fixture = TestBed.createComponent(Hospedeiro);
    if (trilha) fixture.componentInstance.trilha = trilha;
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  };

  it('renderiza título, resumo, ações e conteúdo nos lugares certos', () => {
    const el = montar();
    expect(el.querySelector('h1')?.textContent).toBe('Rex');
    expect(el.querySelector('.titulo [paginaResumo]')?.textContent).toBe('Cachorro · Labrador');
    expect(el.querySelector('.acoes button')?.textContent).toBe('Editar');
    expect(el.querySelector('.conteudo')?.textContent).toBe('Agendamentos');
  });

  it('monta a trilha com links, exceto no item atual', () => {
    const el = montar();
    const links = Array.from(el.querySelectorAll('nz-breadcrumb a')).map((a) => a.textContent);
    expect(links).toEqual(['Tutores', 'Ana']);
    expect(el.querySelector('nz-breadcrumb')?.textContent).toContain('Rex');
  });

  it('omite a trilha em telas de primeiro nível', () => {
    const el = montar([{ rotulo: 'Tutores' }]);
    expect(el.querySelector('nz-breadcrumb')).toBeNull();
  });
});
