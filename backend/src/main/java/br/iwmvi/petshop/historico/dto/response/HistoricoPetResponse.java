package br.iwmvi.petshop.historico.dto.response;

import br.iwmvi.petshop.historico.model.TipoEvento;

import java.time.LocalDateTime;

public record HistoricoPetResponse(
        Long id,
        Long petId,
        TipoEvento tipoEvento,
        String descricao,
        LocalDateTime dataEvento,
        Long funcionarioId,
        String funcionarioNome,
        LocalDateTime createdAt
) {
}
