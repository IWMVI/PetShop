package br.iwmvi.petshop.pagamento.dto.response;

import br.iwmvi.petshop.pagamento.model.MetodoPagamento;
import br.iwmvi.petshop.pagamento.model.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponse(
        Long id,
        Long agendamentoId,
        BigDecimal valor,
        MetodoPagamento metodoPagamento,
        StatusPagamento status,
        LocalDateTime dataPagamento
) {
}
