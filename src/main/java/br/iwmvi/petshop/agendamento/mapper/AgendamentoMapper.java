package br.iwmvi.petshop.agendamento.mapper;

import br.iwmvi.petshop.agendamento.dto.request.AgendamentoRequest;
import br.iwmvi.petshop.agendamento.dto.response.AgendamentoResponse;
import br.iwmvi.petshop.agendamento.dto.response.AgendamentoServicoResponse;
import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.agendamento.model.AgendamentoStatus;
import br.iwmvi.petshop.pet.model.Pet;
import br.iwmvi.petshop.servico.model.Servico;

import java.math.BigDecimal;
import java.util.List;

public final class AgendamentoMapper {

    private AgendamentoMapper() {
    }

    public static Agendamento toEntity(AgendamentoRequest request, Pet pet, List<Servico> servicos) {
        BigDecimal valorTotal = servicos.stream()
                .map(Servico::getPreco)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Agendamento agendamento = new Agendamento(
                pet,
                request.dataHora(),
                request.observacoes(),
                AgendamentoStatus.AGENDADO,
                valorTotal
        );

        agendamento.definirServicos(servicos);

        return agendamento;
    }

    public static AgendamentoResponse toResponse(Agendamento agendamento) {
        List<AgendamentoServicoResponse> servicos = agendamento.getAgendamentoServicos().stream()
                .map(agendamentoServico -> new AgendamentoServicoResponse(
                        agendamentoServico.getServico() != null
                                ? agendamentoServico.getServico().getId()
                                : agendamentoServico.getId().getServicoId(),
                        agendamentoServico.getServico() != null
                                ? agendamentoServico.getServico().getNome()
                                : null,
                        agendamentoServico.getPrecoCobrado()
                ))
                .toList();

        return new AgendamentoResponse(
                agendamento.getId(),
                agendamento.getPet().getId(),
                agendamento.getDataHora(),
                agendamento.getObservacoes(),
                agendamento.getStatus(),
                agendamento.getValorTotal(),
                servicos
        );
    }
}
