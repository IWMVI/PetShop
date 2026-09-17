package br.iwmvi.petshop.agendamento.dto.response;

import java.math.BigDecimal;

public record AgendamentoServicoResponse(
        Long servicoId,
        BigDecimal precoCobrado
) {
}
