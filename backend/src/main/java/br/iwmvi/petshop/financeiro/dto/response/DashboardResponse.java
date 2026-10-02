package br.iwmvi.petshop.financeiro.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal aReceberHoje,
        BigDecimal aPagarHoje,
        BigDecimal totalVencidoReceber,
        BigDecimal totalVencidoPagar,
        BigDecimal percentualRecebidoMes,
        BigDecimal percentualPagoMes,
        List<PontoFluxoCaixaResponse> fluxoCaixa
) {
}
