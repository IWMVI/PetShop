package br.iwmvi.petshop.agendamento.dto.response;

import java.math.BigDecimal;

public record AgendamentoServicoResponse(
        Long servicoId,
        String nome,
        BigDecimal precoCobrado
) {
}
