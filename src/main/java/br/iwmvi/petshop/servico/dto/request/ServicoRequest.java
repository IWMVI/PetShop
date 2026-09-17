package br.iwmvi.petshop.servico.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ServicoRequest(

        @NotBlank
        @Size(max = 150)
        String nome,

        @Size(max = 500)
        String descricao,

        @NotNull
        @DecimalMin("0.01")
        @DecimalMax("9999.99")
        BigDecimal preco,

        @PositiveOrZero
        Integer tempoEstimadoMinutos
) {
}
