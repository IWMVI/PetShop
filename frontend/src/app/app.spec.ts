import { TestBed } from '@angular/core/testing';
import { provideTestEnv } from '../testing/providers';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: provideTestEnv(),
    }).compileComponents();
  });

  it('renderiza o menu com Tutores e Serviços', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const itens = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll('[nz-menu-item]'),
    ).map((a) => a.textContent?.trim());
    expect(itens).toEqual(['Tutores', 'Serviços']);
  });

  it('renderiza os ícones Lucide como SVG', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).querySelector('.brand svg')).not.toBeNull();
  });
});
