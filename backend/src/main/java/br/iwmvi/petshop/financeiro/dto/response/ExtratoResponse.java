package br.iwmvi.petshop.financeiro.dto.response;

import br.iwmvi.petshop.common.dto.PaginaResponse;

import java.math.BigDecimal;

public record ExtratoResponse(
        PaginaResponse<LancamentoFinanceiroResponse> lancamentos,
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal saldo
) {
}
