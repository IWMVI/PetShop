package br.iwmvi.petshop.historico.dto.request;

import br.iwmvi.petshop.historico.model.TipoEvento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;

public record HistoricoPetRequest(

        @NotNull
        TipoEvento tipoEvento,

        @NotBlank
        String descricao,

        @NotNull
        @PastOrPresent
        LocalDateTime dataEvento,

        /* Opcional: eventos externos (ex.: vacina em outra clínica) não têm funcionário. */
        Long funcionarioId
) {
}
