import {
  Component,
  computed,
  DestroyRef,
  ElementRef,
  OnInit,
  inject,
  input,
  numberAttribute,
  signal,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { catchError, distinctUntilChanged, filter, map, of, switchMap, tap } from 'rxjs';
import { TutorApi, mensagemDeErro } from '../../../core/api';
import { CepService, EnderecoCep } from '../../../core/cep/cep.service';
import { TutorRequest, TutorResumo } from '../../../core/models';
import { formatarCpf, vazioParaNull } from '../../../shared/format';
import { MASCARAS, aplicarMascara } from '../../../shared/mascara/mascara';
import { MascaraDirective } from '../../../shared/mascara/mascara.directive';
import { ToastService } from '../../../shared/toast/toast.service';
import { ItemTrilha, Pagina } from '../../../shared/pagina/pagina';
import { cpfValidator, cpfValido } from '../../../shared/validators/cpf.validator';

type StatusCep = 'ocioso' | 'buscando' | 'encontrado' | 'nao-encontrado';

@Component({
  selector: 'app-tutor-form',
  imports: [
    Pagina,
    ReactiveFormsModule,
    RouterLink,
    LucideAngularModule,
    MascaraDirective,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzDescriptionsModule,
    NzFormModule,
    NzGridModule,
    NzInputModule,
    NzModalModule,
  ],
  templateUrl: './tutor-form.html',
  styleUrl: './tutor-form.scss',
})
export class TutorForm implements OnInit {
  readonly id = input(undefined, { transform: numberAttribute });

  protected readonly titulo = computed(() => (this.id() ? 'Editar tutor' : 'Novo tutor'));
  protected readonly trilha = computed<ItemTrilha[]>(() => [
    { rotulo: 'Tutores', link: '/tutores' },
    { rotulo: this.id() ? 'Editar' : 'Novo' },
  ]);
  private readonly api = inject(TutorApi);
  private readonly cepService = inject(CepService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly salvando = signal(false);
  protected readonly statusCep = signal<StatusCep>('ocioso');
  protected readonly mascaras = MASCARAS;
  /** Tutor que já usa o CPF informado; quando preenchido, abre o modal de recuperação. */
  protected readonly cpfExistente = signal<TutorResumo | null>(null);
  protected readonly restaurando = signal(false);

  protected readonly form = this.fb.group({
    nome: ['', Validators.required],
    cpf: ['', [Validators.required, cpfValidator]],
    email: ['', [Validators.required, Validators.email]],
    telefone: ['', [Validators.required, Validators.pattern(/^\(\d{2}\) \d{4,5}-\d{4}$/)]],
    endereco: this.fb.group({
      cep: ['', [Validators.required, Validators.pattern(/^\d{5}-?\d{3}$/)]],
      logradouro: ['', Validators.required],
      numero: [''],
      complemento: [''],
      bairro: ['', Validators.required],
      cidade: ['', Validators.required],
      estado: ['', Validators.pattern(/^[A-Za-z]{2}$/)],
    }),
  });

  ngOnInit() {
    this.buscarEnderecoAoInformarCep();
    this.verificarCpfAoInformar();

    const id = this.id();
    if (!id) return;
    this.api.buscar(id).subscribe({
      next: (t) =>
        // emitEvent: false evita que o CEP carregado dispare a busca e sobrescreve o endereço salvo.
        this.form.patchValue(
          {
            ...t,
            cpf: t.cpf ? formatarCpf(t.cpf) : '',
            telefone: aplicarMascara(t.telefone, MASCARAS.telefone),
            endereco: {
              ...t.endereco,
              cep: aplicarMascara(t.endereco.cep, MASCARAS.cep),
              numero: t.endereco.numero ?? '',
              complemento: t.endereco.complemento ?? '',
              estado: t.endereco.estado ?? '',
            },
          },
          { emitEvent: false },
        ),
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }

  salvar() {
    if (this.form.invalid) {
      this.marcarInvalidos();
      return;
    }
    const v = this.form.getRawValue();
    const body: TutorRequest = {
      ...v,
      cpf: v.cpf.replace(/\D/g, ''),
      telefone: v.telefone.replace(/\D/g, ''),
      endereco: {
        ...v.endereco,
        numero: vazioParaNull(v.endereco.numero),
        complemento: vazioParaNull(v.endereco.complemento),
        estado: vazioParaNull(v.endereco.estado.toUpperCase()),
      },
    };
    const id = this.id();
    this.salvando.set(true);
    (id ? this.api.atualizar(id, body) : this.api.criar(body)).subscribe({
      next: (t) => {
        this.toast.sucesso(id ? 'Tutor atualizado.' : 'Tutor cadastrado.');
        this.router.navigate(['/tutores', t.id]);
      },
      error: (e) => {
        this.salvando.set(false);
        if (e instanceof HttpErrorResponse && e.status === 409 && /CPF/i.test(mensagemDeErro(e))) {
          this.api.buscarPorCpf(v.cpf).subscribe({
            next: (t) => this.cpfExistente.set(t),
            error: () => this.toast.erro(mensagemDeErro(e)),
          });
          return;
        }
        this.toast.erro(mensagemDeErro(e));
      },
    });
  }

  restaurarCadastro() {
    const tutor = this.cpfExistente();
    if (!tutor) return;
    this.restaurando.set(true);
    this.api.restaurar(tutor.id).subscribe({
      next: () => {
        this.restaurando.set(false);
        this.cpfExistente.set(null);
        this.toast.sucesso('Cadastro restaurado. Revise os dados do tutor.');
        this.router.navigate(['/tutores', tutor.id, 'editar']);
      },
      error: (e) => {
        this.restaurando.set(false);
        this.toast.erro(mensagemDeErro(e));
      },
    });
  }

  verCadastro() {
    const tutor = this.cpfExistente();
    if (!tutor) return;
    this.cpfExistente.set(null);
    this.router.navigate(['/tutores', tutor.id]);
  }

  informarOutroCpf() {
    this.cpfExistente.set(null);
    this.form.controls.cpf.setValue('');
    this.host.nativeElement.querySelector<HTMLInputElement>('#cpf')?.focus();
  }

  /** Ao completar um CPF válido, verifica se ele já pertence a um tutor (ativo ou excluído). */
  private verificarCpfAoInformar() {
    this.form.controls.cpf.valueChanges
      .pipe(
        map((cpf) => cpf.replace(/\D/g, '')),
        distinctUntilChanged(),
        filter((cpf) => cpfValido(cpf)),
        switchMap((cpf) => this.api.buscarPorCpf(cpf).pipe(catchError(() => of(null)))),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((tutor) => {
        // Na edição, o próprio CPF do tutor não conta como conflito.
        if (tutor && tutor.id !== this.id()) this.cpfExistente.set(tutor);
      });
  }

  /** Quando o CEP fica completo, consulta o ViaCEP e preenche o endereço. */
  private buscarEnderecoAoInformarCep() {
    const endereco = this.form.controls.endereco;
    endereco.controls.cep.valueChanges
      .pipe(
        map((cep) => cep.replace(/\D/g, '')),
        distinctUntilChanged(),
        tap(() => this.statusCep.set('ocioso')),
        filter((cep) => cep.length === 8),
        tap(() => this.statusCep.set('buscando')),
        switchMap((cep) => this.cepService.buscar(cep)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((resultado) => {
        if (!resultado) {
          this.statusCep.set('nao-encontrado');
          return;
        }
        this.preencherEndereco(resultado);
        this.statusCep.set('encontrado');
      });
  }

  private preencherEndereco(r: EnderecoCep) {
    const endereco = this.form.controls.endereco;
    endereco.patchValue({
      logradouro: r.logradouro || endereco.value.logradouro,
      bairro: r.bairro || endereco.value.bairro,
      cidade: r.cidade,
      estado: r.estado,
      complemento: endereco.value.complemento || r.complemento,
    });
    // CEPs "gerais" (cidade inteira) não trazem logradouro: foca nele; senão, no número.
    const proximo = r.logradouro ? 'numero' : 'logradouro';
    this.host.nativeElement.querySelector<HTMLInputElement>(`#${proximo}`)?.focus();
  }

  /** O nz-form-control só exibe o erro de controles "dirty". */
  private marcarInvalidos() {
    const controles = [
      ...Object.values(this.form.controls),
      ...Object.values(this.form.controls.endereco.controls),
    ];
    for (const c of controles) {
      c.markAsDirty();
      c.updateValueAndValidity({ onlySelf: true });
    }
  }
}
