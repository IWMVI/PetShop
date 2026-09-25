import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { filter } from 'rxjs';
import { SituacaoApi, StatusApiService } from './core/status-api/status-api.service';
import { Status, TomStatus } from './shared/status/status';
import { TelaService } from './shared/tela/tela.service';

const STATUS_API: Record<SituacaoApi, { tom: TomStatus; rotulo: string }> = {
  verificando: { tom: 'neutro', rotulo: 'Verificando API…' },
  online: { tom: 'sucesso', rotulo: 'API online' },
  instavel: { tom: 'alerta', rotulo: 'API instável' },
  offline: { tom: 'erro', rotulo: 'API offline' },
};

@Component({
  selector: 'app-root',
  imports: [
    NgTemplateOutlet,
    RouterOutlet,
    RouterLink,
    LucideAngularModule,
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

  protected readonly recolhido = signal(false);
  /** Gaveta do menu no layout de celular. */
  protected readonly menuAberto = signal(false);

  protected readonly menu = [
    { rota: '/tutores', rotulo: 'Tutores', icone: 'users' },
    { rota: '/servicos', rotulo: 'Serviços', icone: 'wrench' },
    { rota: '/funcionarios', rotulo: 'Funcionários', icone: 'id-card' },
  ];

  protected readonly api = computed(() => {
    const situacao = this.statusApi.situacao();
    return { ...STATUS_API[situacao], pulsando: situacao === 'verificando' };
  });

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
  }

  protected fecharMenu() {
    this.menuAberto.set(false);
  }
}
