import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'tutores' },
  {
    path: 'tutores',
    children: [
      {
        path: '',
        title: 'Tutores',
        loadComponent: () =>
          import('./features/tutores/tutor-list/tutor-list').then((m) => m.TutorList),
      },
      {
        path: 'novo',
        title: 'Novo tutor',
        loadComponent: () =>
          import('./features/tutores/tutor-form/tutor-form').then((m) => m.TutorForm),
      },
      {
        path: ':id',
        title: 'Tutor',
        loadComponent: () =>
          import('./features/tutores/tutor-detail/tutor-detail').then((m) => m.TutorDetail),
      },
      {
        path: ':id/editar',
        title: 'Editar tutor',
        loadComponent: () =>
          import('./features/tutores/tutor-form/tutor-form').then((m) => m.TutorForm),
      },
      {
        path: ':tutorId/pets/novo',
        title: 'Novo pet',
        loadComponent: () => import('./features/pets/pet-form/pet-form').then((m) => m.PetForm),
      },
      {
        path: ':tutorId/pets/:petId',
        title: 'Pet',
        loadComponent: () =>
          import('./features/pets/pet-detail/pet-detail').then((m) => m.PetDetail),
      },
      {
        path: ':tutorId/pets/:petId/editar',
        title: 'Editar pet',
        loadComponent: () => import('./features/pets/pet-form/pet-form').then((m) => m.PetForm),
      },
      {
        path: ':tutorId/pets/:petId/historico/novo',
        title: 'Novo evento',
        loadComponent: () =>
          import('./features/historico/historico-form/historico-form').then((m) => m.HistoricoForm),
      },
      {
        path: ':tutorId/pets/:petId/agendamentos/novo',
        title: 'Novo agendamento',
        loadComponent: () =>
          import('./features/agendamentos/agendamento-form/agendamento-form').then(
            (m) => m.AgendamentoForm,
          ),
      },
      {
        path: ':tutorId/pets/:petId/agendamentos/:agendamentoId',
        title: 'Agendamento',
        loadComponent: () =>
          import('./features/agendamentos/agendamento-detail/agendamento-detail').then(
            (m) => m.AgendamentoDetail,
          ),
      },
      {
        path: ':tutorId/pets/:petId/agendamentos/:id/editar',
        title: 'Reagendar',
        loadComponent: () =>
          import('./features/agendamentos/agendamento-form/agendamento-form').then(
            (m) => m.AgendamentoForm,
          ),
      },
      {
        path: ':tutorId/pets/:petId/agendamentos/:agendamentoId/pagamentos/novo',
        title: 'Registrar pagamento',
        loadComponent: () =>
          import('./features/pagamentos/pagamento-form/pagamento-form').then(
            (m) => m.PagamentoForm,
          ),
      },
    ],
  },
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
  {
    path: 'funcionarios',
    children: [
      {
        path: '',
        title: 'Funcionários',
        loadComponent: () =>
          import('./features/funcionarios/funcionario-list/funcionario-list').then(
            (m) => m.FuncionarioList,
          ),
      },
      {
        path: 'novo',
        title: 'Novo funcionário',
        loadComponent: () =>
          import('./features/funcionarios/funcionario-form/funcionario-form').then(
            (m) => m.FuncionarioForm,
          ),
      },
      {
        path: ':id/editar',
        title: 'Editar funcionário',
        loadComponent: () =>
          import('./features/funcionarios/funcionario-form/funcionario-form').then(
            (m) => m.FuncionarioForm,
          ),
      },
    ],
  },
  {
    path: 'financeiro',
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'extrato' },
      {
        path: 'extrato',
        title: 'Extrato',
        loadComponent: () => import('./features/financeiro/extrato/extrato').then((m) => m.Extrato),
      },
      {
        path: 'novo',
        title: 'Novo lançamento',
        loadComponent: () =>
          import('./features/financeiro/lancamento-form/lancamento-form').then(
            (m) => m.LancamentoForm,
          ),
      },
      {
        path: ':id/editar',
        title: 'Editar lançamento',
        loadComponent: () =>
          import('./features/financeiro/lancamento-form/lancamento-form').then(
            (m) => m.LancamentoForm,
          ),
      },
      {
        path: 'contas-a-pagar',
        title: 'Contas a Pagar',
        data: { tipo: 'SAIDA' },
        loadComponent: () => import('./features/financeiro/contas/contas').then((m) => m.Contas),
      },
      {
        path: 'contas-a-pagar/novo',
        title: 'Nova conta a pagar',
        data: { tipo: 'SAIDA' },
        loadComponent: () =>
          import('./features/financeiro/conta-form/conta-form').then((m) => m.ContaForm),
      },
      {
        path: 'contas-a-pagar/:id/editar',
        title: 'Editar conta a pagar',
        data: { tipo: 'SAIDA' },
        loadComponent: () =>
          import('./features/financeiro/conta-form/conta-form').then((m) => m.ContaForm),
      },
      {
        path: 'contas-a-receber',
        title: 'Contas a Receber',
        data: { tipo: 'ENTRADA' },
        loadComponent: () => import('./features/financeiro/contas/contas').then((m) => m.Contas),
      },
      {
        path: 'contas-a-receber/novo',
        title: 'Nova conta a receber',
        data: { tipo: 'ENTRADA' },
        loadComponent: () =>
          import('./features/financeiro/conta-form/conta-form').then((m) => m.ContaForm),
      },
      {
        path: 'contas-a-receber/:id/editar',
        title: 'Editar conta a receber',
        data: { tipo: 'ENTRADA' },
        loadComponent: () =>
          import('./features/financeiro/conta-form/conta-form').then((m) => m.ContaForm),
      },
    ],
  },
  { path: '**', redirectTo: 'tutores' },
];
