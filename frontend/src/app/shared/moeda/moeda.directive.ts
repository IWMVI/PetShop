import { Directive, ElementRef, HostListener, forwardRef, inject, input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { centavosDigitados, formatarValor } from './moeda';

/**
 * Campo de valor monetário no padrão brasileiro, digitado da direita para a esquerda
 * (0,00 → 0,01 → 0,10 → 1,00 → ... → 1.000,00). O controle recebe um number.
 *
 * Uso: <input nz-input formControlName="preco" appMoeda [maximo]="9999.99" />
 */
@Directive({
  selector: 'input[appMoeda]',
  host: { inputmode: 'numeric', autocomplete: 'off' },
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => MoedaDirective), multi: true },
  ],
})
export class MoedaDirective implements ControlValueAccessor {
  readonly maximo = input(Number.MAX_SAFE_INTEGER / 100);

  private readonly el = inject<ElementRef<HTMLInputElement>>(ElementRef).nativeElement;
  private onChange: (valor: number | null) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  writeValue(valor: number | null | undefined): void {
    this.el.value = valor == null ? '' : formatarValor(Number(valor));
  }

  registerOnChange(fn: (valor: number | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(desabilitado: boolean): void {
    this.el.disabled = desabilitado;
  }

  @HostListener('input')
  aoDigitar() {
    const valor = centavosDigitados(this.el.value, this.maximo());
    this.el.value = valor == null ? '' : formatarValor(valor);
    // Mantém o cursor no fim, já que os dígitos entram pela direita.
    this.el.setSelectionRange(this.el.value.length, this.el.value.length);
    this.onChange(valor);
  }

  @HostListener('blur')
  aoSair() {
    this.onTouched();
  }
}
