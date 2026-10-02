package br.iwmvi.petshop.financeiro.dto.response;

import java.math.BigDecimal;

public record SaldoContasResponse(
        BigDecimal totalPendente,
        BigDecimal totalVencido
) {
}
