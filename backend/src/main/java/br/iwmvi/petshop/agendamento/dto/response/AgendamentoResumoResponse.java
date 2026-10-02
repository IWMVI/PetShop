package br.iwmvi.petshop.agendamento.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resumo de um agendamento para exibição fora do contexto de um pet específico
 * (ex.: dashboard geral do sistema), com nome do pet e do tutor já resolvidos.
 */
public record AgendamentoResumoResponse(
        Long id,
        LocalDateTime dataHora,
        Long petId,
        String petNome,
        Long tutorId,
        String tutorNome,
        BigDecimal valorTotal
) {
}
