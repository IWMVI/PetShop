import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, throwError } from 'rxjs';
import { provideTestEnv } from '../../../../testing/providers';
import { FinanceiroApi } from '../../../core/api';
import { Dashboard as DashboardResponse } from '../../../core/models';
import { formatarMoeda } from '../../../shared/format';
import { Grafico } from '../../../shared/grafico/grafico';
import { Dashboard } from './dashboard';

function resposta(parcial: Partial<DashboardResponse> = {}): DashboardResponse {
  return {
    aReceberHoje: 300,
    aPagarHoje: 150,
    totalVencidoReceber: 0,
    totalVencidoPagar: 0,
    percentualRecebidoMes: 70,
    percentualPagoMes: 40,
    fluxoCaixa: [
      { data: '2026-09-28', entradas: 100, saidas: 40 },
      { data: '2026-09-29', entradas: 50, saidas: 80 },
      { data: '2026-09-30', entradas: 200, saidas: 20 },
    ],
    ...parcial,
  };
}

describe('Dashboard', () => {
  let fixture: ComponentFixture<Dashboard>;
  let api: jest.Mocked<Pick<FinanceiroApi, 'dashboard'>>;

  function criarComponente() {
    fixture = TestBed.createComponent(Dashboard);
    fixture.detectChanges();
  }

  async function montar() {
    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [provideTestEnv(), { provide: FinanceiroApi, useValue: api }],
    }).compileComponents();
    criarComponente();
  }

  const el = () => fixture.nativeElement as HTMLElement;

  beforeEach(() => {
    api = { dashboard: jest.fn().mockReturnValue(of(resposta())) };
  });

  it('carrega e mostra os valores dos cards de hoje', async () => {
    await montar();

    expect(el().textContent).toContain(formatarMoeda(300));
    expect(el().textContent).toContain(formatarMoeda(150));
    expect(el().textContent).toContain('A receber hoje');
    expect(el().textContent).toContain('A pagar hoje');
  });

  it('mostra os percentuais do mês', async () => {
    await montar();

    expect(el().textContent).toContain('70');
    expect(el().textContent).toContain('40');
  });

  it('calcula o saldo líquido diário (entradas - saídas) e passa ao gráfico de linha', async () => {
    await montar();

    const graficos = fixture.debugElement.queryAll(By.directive(Grafico));
    const fluxoCaixa = graficos.find((g) => g.componentInstance.tipo() === 'line')!;

    expect(fluxoCaixa.componentInstance.dados().datasets[0].data).toEqual([60, -30, 180]);
    expect(fluxoCaixa.componentInstance.dados().labels).toEqual(['28/09', '29/09', '30/09']);
  });

  it('passa as entradas diárias ao gráfico de barras', async () => {
    await montar();

    const graficos = fixture.debugElement.queryAll(By.directive(Grafico));
    const entradas = graficos.find((g) => g.componentInstance.tipo() === 'bar')!;

    expect(entradas.componentInstance.dados().datasets[0].data).toEqual([100, 50, 200]);
  });

  it('mostra aviso de vencido quando há valor vencido a receber ou a pagar', async () => {
    api.dashboard.mockReturnValue(of(resposta({ totalVencidoReceber: 80, totalVencidoPagar: 20 })));
    await montar();

    expect(el().textContent).toContain(`${formatarMoeda(80)} vencido`);
    expect(el().textContent).toContain(`${formatarMoeda(20)} vencido`);
  });

  it('não mostra aviso de vencido quando o total vencido é zero', async () => {
    await montar();

    expect(el().textContent).not.toContain('vencido');
  });

  it('trata erro da API mostrando a mensagem', async () => {
    api.dashboard.mockReturnValue(throwError(() => new Error('falhou')));
    await montar();

    expect(el().querySelector('nz-alert')).not.toBeNull();
  });
});
