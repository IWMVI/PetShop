import { Component, ElementRef, OnChanges, OnDestroy, input, viewChild } from '@angular/core';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';

Chart.register(...registerables);

/**
 * Encapsula um gráfico do Chart.js num canvas, controlando o ciclo de vida manualmente
 * (sem wrapper Angular): recria o chart a cada mudança de `dados`/`tipo`/`opcoes` e
 * destrói a instância anterior antes, para não vazar canvas nem listeners.
 *
 * O container precisa de uma altura definida via CSS (ex.: `height: 240px`), pois
 * `maintainAspectRatio: false` é sempre aplicado para o gráfico ocupar o espaço disponível.
 */
@Component({
  selector: 'app-grafico',
  template: `<canvas #canvas></canvas>`,
  styleUrl: './grafico.scss',
})
export class Grafico implements OnChanges, OnDestroy {
  readonly tipo = input.required<ChartType>();
  readonly dados = input.required<ChartConfiguration['data']>();
  readonly opcoes = input<ChartConfiguration['options']>();

  private readonly canvasRef = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');
  private chart?: Chart;

  ngOnChanges() {
    this.chart?.destroy();
    this.chart = new Chart(this.canvasRef().nativeElement, {
      type: this.tipo(),
      data: this.dados(),
      options: { responsive: true, maintainAspectRatio: false, ...this.opcoes() },
    });
  }

  ngOnDestroy() {
    this.chart?.destroy();
  }
}
