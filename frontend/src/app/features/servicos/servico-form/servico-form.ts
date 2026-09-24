import { Component, computed, OnInit, inject, input, numberAttribute, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { ServicoApi, mensagemDeErro } from '../../../core/api';
import { ServicoRequest } from '../../../core/models';
import { vazioParaNull } from '../../../shared/format';
import { MoedaDirective } from '../../../shared/moeda/moeda.directive';
import { ToastService } from '../../../shared/toast/toast.service';
import { ItemTrilha, Pagina } from '../../../shared/pagina/pagina';

@Component({
  selector: 'app-servico-form',
  imports: [
    Pagina,
    ReactiveFormsModule,
    RouterLink,
    LucideAngularModule,
    MoedaDirective,
    NzButtonModule,
    NzCardModule,
    NzFormModule,
    NzGridModule,
    NzInputModule,
    NzInputNumberModule,
  ],
  templateUrl: './servico-form.html',
  styleUrl: './servico-form.scss',
})
export class ServicoForm implements OnInit {
  readonly id = input(undefined, { transform: numberAttribute });

  protected readonly titulo = computed(() => (this.id() ? 'Editar serviço' : 'Novo serviço'));
  protected readonly trilha = computed<ItemTrilha[]>(() => [
    { rotulo: 'Serviços', link: '/servicos' },
    { rotulo: this.id() ? 'Editar' : 'Novo' },
  ]);
  private readonly api = inject(ServicoApi);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  protected readonly salvando = signal(false);

  protected readonly form = inject(FormBuilder).group({
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    descricao: ['', Validators.maxLength(500)],
    preco: [
      null as number | null,
      [Validators.required, Validators.min(0.01), Validators.max(9999.99)],
    ],
    tempoEstimadoMinutos: [null as number | null, Validators.min(0)],
  });

  ngOnInit() {
    const id = this.id();
    if (!id) return;
    this.api.buscar(id).subscribe({
      next: (s) => this.form.patchValue(s),
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
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
    const body: ServicoRequest = {
      nome: v.nome!,
      descricao: vazioParaNull(v.descricao),
      preco: v.preco!,
      tempoEstimadoMinutos: vazioParaNull(v.tempoEstimadoMinutos),
    };
    const id = this.id();
    this.salvando.set(true);
    (id ? this.api.atualizar(id, body) : this.api.criar(body)).subscribe({
      next: () => {
        this.toast.sucesso(id ? 'Serviço atualizado.' : 'Serviço cadastrado.');
        this.router.navigate(['/servicos']);
      },
      error: (e) => {
        this.salvando.set(false);
        this.toast.erro(mensagemDeErro(e));
      },
    });
  }
}
