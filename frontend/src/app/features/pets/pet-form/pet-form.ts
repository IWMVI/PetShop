import { Component, computed, OnInit, inject, input, numberAttribute, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { NzAutocompleteModule } from 'ng-zorro-antd/auto-complete';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { PetApi, mensagemDeErro } from '../../../core/api';
import { PetRequest } from '../../../core/models';
import { vazioParaNull } from '../../../shared/format';
import { ToastService } from '../../../shared/toast/toast.service';
import { ItemTrilha, Pagina } from '../../../shared/pagina/pagina';

@Component({
  selector: 'app-pet-form',
  imports: [
    Pagina,
    ReactiveFormsModule,
    RouterLink,
    LucideAngularModule,
    NzAutocompleteModule,
    NzButtonModule,
    NzCardModule,
    NzFormModule,
    NzGridModule,
    NzInputModule,
    NzInputNumberModule,
  ],
  templateUrl: './pet-form.html',
  styleUrl: './pet-form.scss',
})
export class PetForm implements OnInit {
  readonly tutorId = input.required({ transform: numberAttribute });
  readonly petId = input(undefined, { transform: numberAttribute });

  protected readonly titulo = computed(() => (this.petId() ? 'Editar pet' : 'Novo pet'));
  protected readonly trilha = computed<ItemTrilha[]>(() => [
    { rotulo: 'Tutores', link: '/tutores' },
    { rotulo: 'Tutor', link: ['/tutores', this.tutorId()] },
    { rotulo: this.petId() ? 'Editar pet' : 'Novo pet' },
  ]);
  private readonly api = inject(PetApi);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  protected readonly salvando = signal(false);
  protected readonly especies = ['Cachorro', 'Gato', 'Pássaro', 'Roedor', 'Réptil'];

  protected readonly form = inject(FormBuilder).group({
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    especie: ['', [Validators.required, Validators.maxLength(50)]],
    raca: ['', Validators.maxLength(100)],
    idade: [null as number | null, Validators.min(0)],
    peso: [null as number | null, [Validators.min(0.01), Validators.max(999.99)]],
  });

  cancelarLink() {
    const base = ['/tutores', this.tutorId()];
    return this.petId() ? [...base, 'pets', this.petId()] : base;
  }

  ngOnInit() {
    const petId = this.petId();
    if (!petId) return;
    this.api.buscar(this.tutorId(), petId).subscribe({
      next: (p) => this.form.patchValue(p),
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
    const body: PetRequest = {
      nome: v.nome!,
      especie: v.especie!,
      raca: vazioParaNull(v.raca),
      idade: vazioParaNull(v.idade),
      peso: vazioParaNull(v.peso),
    };
    const tutorId = this.tutorId();
    const petId = this.petId();
    this.salvando.set(true);
    (petId ? this.api.atualizar(tutorId, petId, body) : this.api.criar(tutorId, body)).subscribe({
      next: (p) => {
        this.toast.sucesso(petId ? 'Pet atualizado.' : 'Pet cadastrado.');
        this.router.navigate(['/tutores', tutorId, 'pets', p.id]);
      },
      error: (e) => {
        this.salvando.set(false);
        this.toast.erro(mensagemDeErro(e));
      },
    });
  }
}
