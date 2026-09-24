import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTestEnv } from '../testing/providers';
import { App } from './app';
import { TelaService } from './shared/tela/tela.service';

describe('App', () => {
  const celular = signal(false);
  let fixture: ComponentFixture<App>;

  beforeEach(async () => {
    celular.set(false);
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideTestEnv(), { provide: TelaService, useValue: { celular } }],
    }).compileComponents();
    fixture = TestBed.createComponent(App);
    fixture.detectChanges();
  });

  const el = () => fixture.nativeElement as HTMLElement;
  it('no desktop exibe o menu lateral com Tutores e Serviços', () => {
    const itens = Array.from(el().querySelectorAll('nz-sider [nz-menu-item]')).map((a) =>
      a.textContent?.trim(),
    );
    expect(itens).toEqual(['Tutores', 'Serviços']);
    expect(el().querySelector('.topo')).toBeNull();
  });

  it('renderiza os ícones Lucide como SVG', () => {
    expect(el().querySelector('.marca-app svg')).not.toBeNull();
  });

  it('no celular troca o menu lateral por barra superior e gaveta', async () => {
    celular.set(true);
    fixture.detectChanges();

    expect(el().querySelector('nz-sider')).toBeNull();
    const botao = el().querySelector<HTMLButtonElement>('button[aria-label="Abrir menu"]')!;
    expect(botao.getAttribute('aria-expanded')).toBe('false');

    botao.click();
    fixture.detectChanges();
    // A verificação periódica da API mantém um timer ativo, então whenStable() não resolve:
    // espera um instante real pela animação de abertura da gaveta.
    await new Promise((r) => setTimeout(r, 100));
    fixture.detectChanges();

    expect(botao.getAttribute('aria-expanded')).toBe('true');
    const gaveta = document.querySelector('.menu-celular')!;
    expect(
      Array.from(gaveta.querySelectorAll('[nz-menu-item]')).map((i) => i.textContent?.trim()),
    ).toEqual(['Tutores', 'Serviços']);
  });
});
