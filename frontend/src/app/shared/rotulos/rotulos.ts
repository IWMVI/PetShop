import { Cargo, TipoEvento } from '../../core/models';
import { TomStatus } from '../status/status';

/** Rótulos dos cargos, na ordem em que aparecem nos formulários. */
export const CARGOS: Record<Cargo, string> = {
  VETERINARIO: 'Veterinário(a)',
  TOSADOR: 'Tosador(a)',
  BANHISTA: 'Banhista',
  ATENDENTE: 'Atendente',
  GERENTE: 'Gerente',
};

/** Rótulo e tom de cada tipo de evento do histórico, exibidos com o <app-status>. */
export const TIPOS_EVENTO: Record<TipoEvento, { rotulo: string; tom: TomStatus }> = {
  VACINACAO: { rotulo: 'Vacinação', tom: 'sucesso' },
  CONSULTA: { rotulo: 'Consulta', tom: 'destaque' },
  PROCEDIMENTO: { rotulo: 'Procedimento', tom: 'alerta' },
  SERVICO: { rotulo: 'Serviço', tom: 'neutro' },
  OUTRO: { rotulo: 'Outro', tom: 'neutro' },
};

/** Lista de opções {valor, rótulo} para selects, preservando a ordem de declaração. */
export function opcoes<T extends string>(mapa: Record<T, string | { rotulo: string }>) {
  return (Object.entries(mapa) as [T, string | { rotulo: string }][]).map(([valor, r]) => ({
    valor,
    rotulo: typeof r === 'string' ? r : r.rotulo,
  }));
}
