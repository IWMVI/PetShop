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
        path: ':tutorId/pets/:petId/agendamentos/novo',
        title: 'Novo agendamento',
        loadComponent: () =>
          import('./features/agendamentos/agendamento-form/agendamento-form').then(
            (m) => m.AgendamentoForm,
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
  { path: '**', redirectTo: 'tutores' },
];
