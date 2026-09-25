import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTestEnv } from '../testing/providers';
import { App } from './app';
import { TelaService } from './shared/tela/tela.service';

describe('App', () => {
  const celular = signal(false);
  let fixture: ComponentFixture<App>;
  let http: HttpTestingController;

  beforeEach(async () => {
    celular.set(false);
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideTestEnv(),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: TelaService, useValue: { celular } },
      ],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(App);
    fixture.detectChanges();
  });

  const el = () => fixture.nativeElement as HTMLElement;
  /** Texto do status: visível com o menu aberto, ou rótulo acessível com o menu recolhido. */
  const textoStatus = (status: Element) =>
    status.textContent?.trim() || status.getAttribute('aria-label') || '';
  const responderSaude = (corpo: object) => {
    http.expectOne('/api/actuator/health').flush(corpo);
    fixture.detectChanges();
  };

  it('no desktop exibe o menu lateral com Tutores, Serviços e Funcionários', () => {
    const itens = Array.from(el().querySelectorAll('nz-sider [nz-menu-item]')).map((a) =>
      a.textContent?.trim(),
    );
    expect(itens).toEqual(['Tutores', 'Serviços', 'Funcionários']);
    expect(el().querySelector('.topo')).toBeNull();
  });

  it('renderiza os ícones Lucide como SVG', () => {
    expect(el().querySelector('.marca-app svg')).not.toBeNull();
  });

  it('mostra o status da API no menu', async () => {
    const status = () => el().querySelector('.status-api app-status')!;
    expect(textoStatus(status())).toBe('Verificando API…');
    await new Promise((r) => setTimeout(r));
    responderSaude({ status: 'UP' });
    expect(textoStatus(status())).toBe('API online');
    expect(status().classList).toContain('status-sucesso');
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
    ).toEqual(['Tutores', 'Serviços', 'Funcionários']);
  });
});
