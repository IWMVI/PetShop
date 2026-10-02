package br.iwmvi.petshop.pagamento.mapper;

import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.pagamento.dto.request.PagamentoRequest;
import br.iwmvi.petshop.pagamento.dto.response.PagamentoResponse;
import br.iwmvi.petshop.pagamento.model.Pagamento;

public final class PagamentoMapper {

    private PagamentoMapper() {
    }

    public static Pagamento toEntity(PagamentoRequest request, Agendamento agendamento) {
        return new Pagamento(agendamento, request.valor(), request.metodoPagamento());
    }

    public static PagamentoResponse toResponse(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getAgendamento().getId(),
                pagamento.getValor(),
                pagamento.getMetodoPagamento(),
                pagamento.getStatus(),
                pagamento.getDataPagamento()
        );
    }
}
