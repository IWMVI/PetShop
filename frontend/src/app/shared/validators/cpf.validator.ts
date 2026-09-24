import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Verifica se o CPF (com ou sem máscara) tem 11 dígitos e dígitos verificadores corretos. */
export function cpfValido(cpf: string): boolean {
  const digitos = cpf.replace(/\D/g, '');
  if (digitos.length !== 11 || /^(\d)\1{10}$/.test(digitos)) return false;

  const verificador = (base: string) => {
    const soma = [...base].reduce((acc, d, i) => acc + Number(d) * (base.length + 1 - i), 0);
    const resto = (soma * 10) % 11;
    return resto === 10 ? 0 : resto;
  };

  const d1 = verificador(digitos.slice(0, 9));
  const d2 = verificador(digitos.slice(0, 9) + d1);
  return digitos.endsWith(`${d1}${d2}`);
}

/** Validador de formulário; campos vazios ficam a cargo do Validators.required. */
export const cpfValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const valor = control.value as string | null;
  if (!valor) return null;
  return cpfValido(valor) ? null : { cpf: true };
};
