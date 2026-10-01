const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const dataHora = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' });

export function formatarMoeda(valor: number | null | undefined): string {
  return valor == null ? '—' : moeda.format(valor);
}

export function formatarDataHora(iso: string): string {
  return dataHora.format(new Date(iso));
}

const peso = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 });

export function formatarPeso(kg: number): string {
  return `${peso.format(kg)} kg`;
}

export function formatarTelefone(tel: string): string {
  const d = tel.replace(/\D/g, '');
  if (d.length === 11) return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`;
  if (d.length === 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`;
  return tel;
}

export function formatarCpf(cpf: string | null | undefined): string {
  const d = (cpf ?? '').replace(/\D/g, '');
  return d.length === 11 ? `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}` : '—';
}

/** Converte um Date para o formato LocalDateTime do back-end ("2026-09-23T10:00:00"), no fuso local. */
export function paraLocalDateTime(data: Date): string {
  const p = (n: number) => String(n).padStart(2, '0');
  return (
    `${data.getFullYear()}-${p(data.getMonth() + 1)}-${p(data.getDate())}` +
    `T${p(data.getHours())}:${p(data.getMinutes())}:00`
  );
}

/** Converte string vazia em null, para campos opcionais. */
export function vazioParaNull<T>(v: T | '' | null | undefined): T | null {
  return v === '' || v === undefined ? null : v;
}
