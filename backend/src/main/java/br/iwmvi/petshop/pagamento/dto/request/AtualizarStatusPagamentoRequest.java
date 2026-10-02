package br.iwmvi.petshop.pagamento.dto.request;

import br.iwmvi.petshop.pagamento.model.StatusPagamento;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AtualizarStatusPagamentoRequest(
        @NotNull StatusPagamento status,
        LocalDateTime dataPagamento
) {
}
