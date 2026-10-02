import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzTableModule } from 'ng-zorro-antd/table';
import { ConsultaExtrato, FinanceiroApi, TAMANHO_PAGINA, mensagemDeErro } from '../../../core/api';
import { CategoriaLancamento, LancamentoFinanceiro, TipoLancamento } from '../../../core/models';
import { ConfirmacaoService } from '../../../shared/confirmacao.service';
import { formatarDataHora, formatarMoeda, paraLocalDateTime } from '../../../shared/format';
import { Pagina } from '../../../shared/pagina/pagina';
import { CATEGORIAS_LANCAMENTO, TIPOS_LANCAMENTO, opcoes } from '../../../shared/rotulos/rotulos';
import { Status } from '../../../shared/status/status';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-extrato',
  imports: [
    Pagina,
    ReactiveFormsModule,
    RouterLink,
    LucideAngularModule,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzDatePickerModule,
    NzDropDownModule,
    NzEmptyModule,
    NzFormModule,
    NzGridModule,
    NzSelectModule,
    NzTableModule,
    Status,
  ],
  templateUrl: './extrato.html',
  styleUrl: './extrato.scss',
})
export class Extrato implements OnInit {
  private readonly api = inject(FinanceiroApi);
  private readonly toast = inject(ToastService);
  private readonly confirmacao = inject(ConfirmacaoService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly filtros = inject(FormBuilder).group({
    inicio: [null as Date | null],
    fim: [null as Date | null],
    tipo: [null as TipoLancamento | null],
    categoria: [null as CategoriaLancamento | null],
  });

  protected readonly itens = signal<LancamentoFinanceiro[]>([]);
  protected readonly total = signal(0);
  protected readonly pagina = signal(0);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly totalEntradas = signal(0);
  protected readonly totalSaidas = signal(0);
  protected readonly saldo = signal(0);

  protected readonly tamanho = TAMANHO_PAGINA;
  protected readonly tipos = opcoes(TIPOS_LANCAMENTO);
  protected readonly categorias = opcoes(CATEGORIAS_LANCAMENTO);
  protected readonly tiposLancamento = TIPOS_LANCAMENTO;
  protected readonly categoriasLancamento = CATEGORIAS_LANCAMENTO;
  protected readonly moeda = formatarMoeda;
  protected readonly dataHora = formatarDataHora;

  ngOnInit() {
    this.carregar();
    this.filtros.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.pagina.set(0);
      this.carregar();
    });
  }

  irPara(indice: number) {
    this.pagina.set(indice);
    this.carregar();
  }

  confirmarCancelamento(l: LancamentoFinanceiro) {
    this.confirmacao.perigo({
      titulo: 'Cancelar este lançamento?',
      conteudo: `${this.categoriasLancamento[l.categoria]} · ${this.moeda(l.valor)}`,
      confirmar: 'Cancelar lançamento',
      cancelar: 'Voltar',
      aoConfirmar: () => this.cancelar(l),
    });
  }

  cancelar(l: LancamentoFinanceiro) {
    this.api.cancelar(l.id).subscribe({
      next: () => {
        this.toast.sucesso('Lançamento cancelado.');
        this.carregar();
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }

  private carregar() {
    this.carregando.set(true);
    const f = this.filtros.getRawValue();
    const consulta: ConsultaExtrato = {
      inicio: f.inicio ? paraLocalDateTime(f.inicio) : undefined,
      fim: f.fim ? paraLocalDateTime(this.fimDoDia(f.fim)) : undefined,
      tipo: f.tipo ?? undefined,
      categoria: f.categoria ?? undefined,
      pagina: this.pagina(),
      tamanho: this.tamanho,
    };
    this.api.extrato(consulta).subscribe({
      next: (r) => {
        this.itens.set(r.lancamentos.itens);
        this.total.set(r.lancamentos.total);
        this.totalEntradas.set(r.totalEntradas);
        this.totalSaidas.set(r.totalSaidas);
        this.saldo.set(r.saldo);
        this.erro.set(null);
        this.carregando.set(false);
      },
      error: (e) => {
        this.erro.set(mensagemDeErro(e));
        this.carregando.set(false);
      },
    });
  }

  /** O filtro de fim é um dia sem hora; incluir o dia inteiro exige ir até o seu último instante. */
  private fimDoDia(d: Date): Date {
    const copia = new Date(d);
    copia.setHours(23, 59, 59, 999);
    return copia;
  }
}
