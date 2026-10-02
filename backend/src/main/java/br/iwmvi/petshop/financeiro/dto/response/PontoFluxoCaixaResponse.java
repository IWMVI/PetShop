package br.iwmvi.petshop.financeiro.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PontoFluxoCaixaResponse(LocalDate data, BigDecimal entradas, BigDecimal saidas) {
}
