package br.iwmvi.petshop.financeiro.mapper;

import br.iwmvi.petshop.financeiro.dto.request.LancamentoFinanceiroRequest;
import br.iwmvi.petshop.financeiro.dto.response.LancamentoFinanceiroResponse;
import br.iwmvi.petshop.financeiro.model.LancamentoFinanceiro;

public final class LancamentoFinanceiroMapper {

    private LancamentoFinanceiroMapper() {
    }

    public static LancamentoFinanceiro toEntity(LancamentoFinanceiroRequest request) {
        return new LancamentoFinanceiro(
                request.tipo(),
                request.categoria(),
                request.descricao(),
                request.valor(),
                request.data()
        );
    }

    public static LancamentoFinanceiroResponse toResponse(LancamentoFinanceiro lancamento) {
        return new LancamentoFinanceiroResponse(
                lancamento.getId(),
                lancamento.getTipo(),
                lancamento.getCategoria(),
                lancamento.getDescricao(),
                lancamento.getValor(),
                lancamento.getData(),
                lancamento.getPagamentoId()
        );
    }
}
