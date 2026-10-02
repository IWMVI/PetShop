import { Component, OnInit, inject, signal } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { RouterLink } from '@angular/router';
import { Dashboard as DashboardResponse, PontoFluxoCaixa } from '../../../core/models';
import { FinanceiroApi, mensagemDeErro } from '../../../core/api';
import { formatarMoeda } from '../../../shared/format';
import { Grafico } from '../../../shared/grafico/grafico';
import { Pagina } from '../../../shared/pagina/pagina';

const FORMATO_DATA_CURTA = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit' });

/** Saudação conforme a hora do dia; o app não tem usuário logado, então é sem nome. */
export function saudacao(hora = new Date().getHours()): string {
  if (hora < 12) return 'Bom dia!';
  if (hora < 18) return 'Boa tarde!';
  return 'Boa noite!';
}

/** Data ISO ("2026-01-15") formatada como "dd/MM", para o eixo X dos gráficos. */
function dataCurta(iso: string): string {
  return FORMATO_DATA_CURTA.format(new Date(`${iso}T00:00:00`));
}

// Mesma cor de verde usada em app/shared/status/status.scss e no extrato para entradas/saldo positivo.
const COR_ENTRADA = '#389e0d';

/**
 * Página inicial do módulo Financeiro: visão geral do dia e do mês (a pagar/a receber
 * hoje, percentuais do mês) e dois gráficos com os dados de caixa já existentes —
 * como o pet shop ainda não vende produtos, não há "vendas" para mostrar.
 */
@Component({
  selector: 'app-dashboard',
  imports: [
    Pagina,
    RouterLink,
    Grafico,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzGridModule,
    NzProgressModule,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  private readonly api = inject(FinanceiroApi);

  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly dados = signal<DashboardResponse | null>(null);

  protected readonly saudacao = saudacao();
  protected readonly moeda = formatarMoeda;

  protected readonly graficoFluxoCaixa = signal<ChartConfiguration['data']>({
    labels: [],
    datasets: [],
  });
  protected readonly graficoEntradas = signal<ChartConfiguration['data']>({
    labels: [],
    datasets: [],
  });

  protected readonly opcoesGrafico: ChartConfiguration['options'] = {
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true } },
  };

  ngOnInit() {
    this.carregar();
  }

  private carregar() {
    this.carregando.set(true);
    this.api.dashboard().subscribe({
      next: (r) => {
        this.dados.set(r);
        this.montarGraficos(r.fluxoCaixa);
        this.erro.set(null);
        this.carregando.set(false);
      },
      error: (e) => {
        this.erro.set(mensagemDeErro(e));
        this.carregando.set(false);
      },
    });
  }

  private montarGraficos(pontos: PontoFluxoCaixa[]) {
    const labels = pontos.map((p) => dataCurta(p.data));

    this.graficoFluxoCaixa.set({
      labels,
      datasets: [
        {
          label: 'Saldo líquido',
          data: pontos.map((p) => p.entradas - p.saidas),
          borderColor: COR_ENTRADA,
          backgroundColor: COR_ENTRADA,
          tension: 0.3,
        },
      ],
    });

    this.graficoEntradas.set({
      labels,
      datasets: [
        {
          label: 'Entradas',
          data: pontos.map((p) => p.entradas),
          backgroundColor: COR_ENTRADA,
        },
      ],
    });
  }
}
