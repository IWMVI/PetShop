import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { centavosDigitados, formatarValor } from './moeda';
import { MoedaDirective } from './moeda.directive';

describe('centavosDigitados', () => {
  it.each([
    ['0', 0],
    ['1', 0.01],
    ['10', 0.1],
    ['100', 1],
    ['100000', 1000],
    ['1.234,567', 12345.67],
  ])('"%s" → %d', (texto, esperado) => {
    expect(centavosDigitados(texto, 1e9)).toBe(esperado);
  });

  it('retorna null sem dígitos', () => {
    expect(centavosDigitados('', 100)).toBeNull();
  });

  it('respeita o valor máximo', () => {
    expect(centavosDigitados('99999999', 9999.99)).toBe(9999.99);
  });
});

describe('formatarValor', () => {
  it('usa separador de milhar e duas casas', () => {
    expect(formatarValor(1234.5)).toBe('1.234,50');
    expect(formatarValor(0)).toBe('0,00');
  });
});

@Component({
  imports: [ReactiveFormsModule, MoedaDirective],
  template: `<input [formControl]="controle" appMoeda [maximo]="9999.99" />`,
})
class Hospedeiro {
  controle = new FormControl<number | null>(null);
}

describe('MoedaDirective', () => {
  const montar = () => {
    const fixture = TestBed.createComponent(Hospedeiro);
    fixture.detectChanges();
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    const digitar = (texto: string) => {
      input.value = texto;
      input.dispatchEvent(new Event('input'));
    };
    return { fixture, input, digitar };
  };

  it('preenche da direita para a esquerda', () => {
    const { fixture, input, digitar } = montar();
    const passos: [string, string, number][] = [
      ['1', '0,01', 0.01],
      ['0,010', '0,10', 0.1],
      ['0,100', '1,00', 1],
      ['1,000', '10,00', 10],
      ['10,000', '100,00', 100],
      ['100,000', '1.000,00', 1000],
    ];
    for (const [texto, exibido, valor] of passos) {
      digitar(texto);
      expect(input.value).toBe(exibido);
      expect(fixture.componentInstance.controle.value).toBe(valor);
    }
  });

  it('volta ao apagar dígitos', () => {
    const { fixture, input, digitar } = montar();
    digitar('1.000,0');
    expect(input.value).toBe('100,00');
    digitar('');
    expect(input.value).toBe('');
    expect(fixture.componentInstance.controle.value).toBeNull();
  });

  it('exibe o valor vindo do formulário', () => {
    const { fixture, input } = montar();
    fixture.componentInstance.controle.setValue(75.9);
    expect(input.value).toBe('75,90');
  });
});
