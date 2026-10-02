package br.iwmvi.petshop.financeiro.dto.response;

import br.iwmvi.petshop.financeiro.model.CategoriaLancamento;
import br.iwmvi.petshop.financeiro.model.StatusLancamento;
import br.iwmvi.petshop.financeiro.model.TipoLancamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LancamentoFinanceiroResponse(
        Long id,
        TipoLancamento tipo,
        CategoriaLancamento categoria,
        String descricao,
        BigDecimal valor,
        StatusLancamento status,
        LocalDateTime dataVencimento,
        LocalDateTime dataPagamento,
        boolean vencido,
        Long pagamentoId
) {
}
