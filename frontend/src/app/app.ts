import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { NzBadgeModule, NzBadgeStatusType } from 'ng-zorro-antd/badge';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { filter } from 'rxjs';
import { SituacaoApi, StatusApiService } from './core/status-api/status-api.service';
import { formatarMoeda } from './shared/format';
import { SaldoFinanceiroService } from './shared/saldo-financeiro.service';
import { Status, TomStatus } from './shared/status/status';
import { TelaService } from './shared/tela/tela.service';

const STATUS_API: Record<SituacaoApi, { tom: TomStatus; rotulo: string }> = {
  verificando: { tom: 'neutro', rotulo: 'Verificando API…' },
  online: { tom: 'sucesso', rotulo: 'API online' },
  instavel: { tom: 'alerta', rotulo: 'API instável' },
  offline: { tom: 'erro', rotulo: 'API offline' },
};

/** Qual saldo de contas (a pagar/a receber) um item do submenu Financeiro exibe como badge. */
type ChaveSaldo = 'aPagar' | 'aReceber';

interface ItemMenuFilho {
  rota: string;
  rotulo: string;
  /** Presente apenas nos itens que exibem o badge de saldo pendente. */
  saldo?: ChaveSaldo;
}

interface ItemMenu {
  /** Ausente quando o item só serve de agrupador (tem `filhos`). */
  rota?: string;
  rotulo: string;
  icone: string;
  filhos?: ItemMenuFilho[];
}

@Component({
  selector: 'app-root',
  imports: [
    NgTemplateOutlet,
    RouterOutlet,
    RouterLink,
    LucideAngularModule,
    NzBadgeModule,
    NzButtonModule,
    NzDrawerModule,
    NzLayoutModule,
    NzMenuModule,
    Status,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly celular = inject(TelaService).celular;
  private readonly statusApi = inject(StatusApiService);
  protected readonly saldoFinanceiro = inject(SaldoFinanceiroService);

  protected readonly recolhido = signal(false);
  /** Gaveta do menu no layout de celular. */
  protected readonly menuAberto = signal(false);

  protected readonly menu: ItemMenu[] = [
    { rota: '/', rotulo: 'Dashboard', icone: 'layout-dashboard' },
    { rota: '/tutores', rotulo: 'Tutores', icone: 'users' },
    { rota: '/servicos', rotulo: 'Serviços', icone: 'wrench' },
    { rota: '/funcionarios', rotulo: 'Funcionários', icone: 'id-card' },
    {
      rotulo: 'Financeiro',
      icone: 'wallet',
      filhos: [
        { rota: '/financeiro/extrato', rotulo: 'Extrato' },
        { rota: '/financeiro/contas-a-pagar', rotulo: 'Contas a Pagar', saldo: 'aPagar' },
        { rota: '/financeiro/contas-a-receber', rotulo: 'Contas a Receber', saldo: 'aReceber' },
      ],
    },
  ];

  protected readonly api = computed(() => {
    const situacao = this.statusApi.situacao();
    return { ...STATUS_API[situacao], pulsando: situacao === 'verificando' };
  });

  protected readonly moeda = formatarMoeda;

  constructor() {
    // Fecha a gaveta do celular quando a tela muda (ex.: botão voltar). A navegação inicial
    // (id 1) é ignorada: ela termina de forma assíncrona e fecharia uma gaveta aberta
    // pelo usuário enquanto a primeira tela ainda carrega.
    inject(Router)
      .events.pipe(
        filter((e) => e instanceof NavigationEnd && e.id > 1),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.fecharMenu());

    this.saldoFinanceiro.carregar();
  }

  protected fecharMenu() {
    this.menuAberto.set(false);
  }

  /**
   * Badge do item de submenu com o saldo pendente, com destaque quando há valor vencido.
   * Retorna null (sem badge) quando não há saldo carregado ou não há nada pendente.
   */
  protected badgeDe(chave?: ChaveSaldo): { status: NzBadgeStatusType; texto: string } | null {
    if (!chave) return null;
    const saldo =
      chave === 'aPagar' ? this.saldoFinanceiro.aPagar() : this.saldoFinanceiro.aReceber();
    if (!saldo || saldo.totalPendente <= 0) return null;
    return {
      status: saldo.totalVencido > 0 ? 'error' : 'default',
      texto: this.moeda(saldo.totalPendente),
    };
  }
}
