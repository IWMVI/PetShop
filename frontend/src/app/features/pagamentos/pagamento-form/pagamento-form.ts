import { Component, computed, inject, input, numberAttribute, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { PagamentoApi, mensagemDeErro } from '../../../core/api';
import { MetodoPagamento, PagamentoRequest } from '../../../core/models';
import { MoedaDirective } from '../../../shared/moeda/moeda.directive';
import { ItemTrilha, Pagina } from '../../../shared/pagina/pagina';
import { METODOS_PAGAMENTO, opcoes } from '../../../shared/rotulos/rotulos';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-pagamento-form',
  imports: [
    Pagina,
    ReactiveFormsModule,
    RouterLink,
    MoedaDirective,
    NzButtonModule,
    NzCardModule,
    NzFormModule,
    NzInputModule,
    NzSelectModule,
  ],
  templateUrl: './pagamento-form.html',
  styleUrl: './pagamento-form.scss',
})
export class PagamentoForm {
  readonly tutorId = input.required({ transform: numberAttribute });
  readonly petId = input.required({ transform: numberAttribute });
  readonly agendamentoId = input.required({ transform: numberAttribute });

  protected readonly trilha = computed<ItemTrilha[]>(() => [
    { rotulo: 'Tutores', link: '/tutores' },
    { rotulo: 'Tutor', link: ['/tutores', this.tutorId()] },
    { rotulo: 'Pet', link: ['/tutores', this.tutorId(), 'pets', this.petId()] },
    { rotulo: 'Agendamento', link: this.voltar() },
    { rotulo: 'Registrar pagamento' },
  ]);

  private readonly api = inject(PagamentoApi);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  protected readonly salvando = signal(false);
  protected readonly metodos = opcoes(METODOS_PAGAMENTO);

  protected readonly form = inject(FormBuilder).group({
    valor: [null as number | null, [Validators.required, Validators.min(0.01)]],
    metodoPagamento: [null as MetodoPagamento | null, Validators.required],
  });

  voltar() {
    return ['/tutores', this.tutorId(), 'pets', this.petId(), 'agendamentos', this.agendamentoId()];
  }

  salvar() {
    if (this.form.invalid) {
      for (const c of Object.values(this.form.controls)) {
        c.markAsDirty();
        c.updateValueAndValidity({ onlySelf: true });
      }
      return;
    }
    const v = this.form.getRawValue();
    const body: PagamentoRequest = {
      valor: v.valor!,
      metodoPagamento: v.metodoPagamento!,
    };
    this.salvando.set(true);
    this.api.criar(this.agendamentoId(), body).subscribe({
      next: () => {
        this.toast.sucesso('Pagamento registrado.');
        this.router.navigate(this.voltar());
      },
      error: (e) => {
        this.salvando.set(false);
        this.toast.erro(mensagemDeErro(e));
      },
    });
  }
}
