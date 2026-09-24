package br.iwmvi.petshop.agendamento.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record AgendamentoRequest(

        @NotNull
        @FutureOrPresent
        LocalDateTime dataHora,

        @Size(max = 1000)
        String observacoes,

        @NotEmpty
        List<@NotNull @Positive Long> servicoIds
) {
}
