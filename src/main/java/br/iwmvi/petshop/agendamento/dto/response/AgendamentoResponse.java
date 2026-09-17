package br.iwmvi.petshop.agendamento.dto.response;

import br.iwmvi.petshop.agendamento.model.AgendamentoStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AgendamentoResponse(
        Long id,
        Long petId,
        LocalDateTime dataHora,
        String observacoes,
        AgendamentoStatus status,
        BigDecimal valorTotal,
        List<AgendamentoServicoResponse> servicos
) {
}
