package br.iwmvi.petshop.financeiro.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record MarcarComoPagaRequest(
        @NotNull(message = "A data de pagamento é obrigatória.")
        LocalDateTime dataPagamento
) {
}
