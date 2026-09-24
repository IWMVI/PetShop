import { Directive, ElementRef, HostListener, inject, input } from '@angular/core';
import { NgControl } from '@angular/forms';
import { aplicarMascara } from './mascara';

/**
 * Formata o valor do campo enquanto o usuário digita.
 * Uso: <input formControlName="cpf" [appMascara]="mascaras.cpf" />
 */
@Directive({ selector: 'input[appMascara]' })
export class MascaraDirective {
  readonly appMascara = input.required<string>();
  private readonly el = inject<ElementRef<HTMLInputElement>>(ElementRef).nativeElement;
  private readonly control = inject(NgControl, { self: true });

  @HostListener('input')
  formatar() {
    const formatado = aplicarMascara(this.el.value, this.appMascara());
    if (formatado !== this.el.value) {
      this.el.value = formatado;
      this.control.control?.setValue(formatado);
    }
  }
}
