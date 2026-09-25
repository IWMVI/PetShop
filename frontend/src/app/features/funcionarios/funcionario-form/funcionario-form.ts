import { Component, OnInit, computed, inject, input, numberAttribute, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { FuncionarioApi, mensagemDeErro } from '../../../core/api';
import { Cargo, FuncionarioRequest } from '../../../core/models';
import { formatarCpf } from '../../../shared/format';
import { MASCARAS, aplicarMascara } from '../../../shared/mascara/mascara';
import { MascaraDirective } from '../../../shared/mascara/mascara.directive';
import { ItemTrilha, Pagina } from '../../../shared/pagina/pagina';
import { CARGOS, opcoes } from '../../../shared/rotulos/rotulos';
import { ToastService } from '../../../shared/toast/toast.service';
import { cpfValidator } from '../../../shared/validators/cpf.validator';

@Component({
  selector: 'app-funcionario-form',
  imports: [
    Pagina,
    ReactiveFormsModule,
    RouterLink,
    MascaraDirective,
    NzButtonModule,
    NzCardModule,
    NzFormModule,
    NzGridModule,
    NzInputModule,
    NzSelectModule,
  ],
  templateUrl: './funcionario-form.html',
  styleUrl: './funcionario-form.scss',
})
export class FuncionarioForm implements OnInit {
  readonly id = input(undefined, { transform: numberAttribute });

  protected readonly titulo = computed(() =>
    this.id() ? 'Editar funcionário' : 'Novo funcionário',
  );
  protected readonly trilha = computed<ItemTrilha[]>(() => [
    { rotulo: 'Funcionários', link: '/funcionarios' },
    { rotulo: this.id() ? 'Editar' : 'Novo' },
  ]);
  private readonly api = inject(FuncionarioApi);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  protected readonly salvando = signal(false);
  protected readonly mascaras = MASCARAS;
  protected readonly cargos = opcoes(CARGOS);

  protected readonly form = inject(FormBuilder).group({
    nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(150)]],
    cpf: ['', [Validators.required, cpfValidator]],
    cargo: [null as Cargo | null, Validators.required],
    telefone: ['', [Validators.required, Validators.pattern(/^\(\d{2}\) \d{4,5}-\d{4}$/)]],
  });

  ngOnInit() {
    const id = this.id();
    if (!id) return;
    this.api.buscar(id).subscribe({
      next: (f) =>
        this.form.patchValue({
          ...f,
          cpf: formatarCpf(f.cpf),
          telefone: aplicarMascara(f.telefone, MASCARAS.telefone),
        }),
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
    const body: FuncionarioRequest = {
      nome: v.nome!,
      cpf: v.cpf!.replace(/\D/g, ''),
      cargo: v.cargo!,
      telefone: v.telefone!.replace(/\D/g, ''),
    };
    const id = this.id();
    this.salvando.set(true);
    (id ? this.api.atualizar(id, body) : this.api.criar(body)).subscribe({
      next: () => {
        this.toast.sucesso(id ? 'Funcionário atualizado.' : 'Funcionário cadastrado.');
        this.router.navigate(['/funcionarios']);
      },
      error: (e) => {
        this.salvando.set(false);
        this.toast.erro(mensagemDeErro(e));
      },
    });
  }
}
