import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'servicos',
    children: [
      {
        path: '',
        title: 'Serviços',
        loadComponent: () =>
          import('./features/servicos/servico-list/servico-list').then((m) => m.ServicoList),
      },
      {
        path: 'novo',
        title: 'Novo serviço',
        loadComponent: () =>
          import('./features/servicos/servico-form/servico-form').then((m) => m.ServicoForm),
      },
      {
        path: ':id/editar',
        title: 'Editar serviço',
        loadComponent: () =>
          import('./features/servicos/servico-form/servico-form').then((m) => m.ServicoForm),
      },
    ],
  },
];
