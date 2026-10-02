package br.iwmvi.petshop.pagamento.dto.request;

import br.iwmvi.petshop.pagamento.model.MetodoPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PagamentoRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @NotNull MetodoPagamento metodoPagamento
) {
}
