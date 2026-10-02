package br.iwmvi.petshop.financeiro.mapper;

import br.iwmvi.petshop.financeiro.dto.request.ContaFinanceiraRequest;
import br.iwmvi.petshop.financeiro.dto.request.LancamentoFinanceiroRequest;
import br.iwmvi.petshop.financeiro.dto.response.LancamentoFinanceiroResponse;
import br.iwmvi.petshop.financeiro.model.LancamentoFinanceiro;
import br.iwmvi.petshop.financeiro.model.StatusLancamento;

import java.time.LocalDateTime;

public final class LancamentoFinanceiroMapper {

    private LancamentoFinanceiroMapper() {
    }

    public static LancamentoFinanceiro toEntity(LancamentoFinanceiroRequest request) {
        return new LancamentoFinanceiro(
                request.tipo(),
                request.categoria(),
                request.descricao(),
                request.valor(),
                request.dataPagamento()
        );
    }

    public static LancamentoFinanceiro toEntity(ContaFinanceiraRequest request) {
        return LancamentoFinanceiro.novaConta(
                request.tipo(),
                request.categoria(),
                request.descricao(),
                request.valor(),
                request.dataVencimento()
        );
    }

    public static LancamentoFinanceiroResponse toResponse(LancamentoFinanceiro lancamento) {
        boolean vencido = lancamento.getStatus() == StatusLancamento.PENDENTE
                && lancamento.getDataVencimento() != null
                && lancamento.getDataVencimento().isBefore(LocalDateTime.now());

        return new LancamentoFinanceiroResponse(
                lancamento.getId(),
                lancamento.getTipo(),
                lancamento.getCategoria(),
                lancamento.getDescricao(),
                lancamento.getValor(),
                lancamento.getStatus(),
                lancamento.getDataVencimento(),
                lancamento.getDataPagamento(),
                vencido,
                lancamento.getPagamentoId()
        );
    }
}
