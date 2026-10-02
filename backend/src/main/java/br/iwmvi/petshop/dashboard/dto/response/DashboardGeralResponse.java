package br.iwmvi.petshop.dashboard.dto.response;

import br.iwmvi.petshop.agendamento.dto.response.AgendamentoResumoResponse;
import br.iwmvi.petshop.financeiro.dto.response.DashboardResponse;

import java.util.List;

/** Dados da página inicial do sistema: resumo financeiro e agendamentos de hoje/próximos. */
public record DashboardGeralResponse(
        DashboardResponse financeiro,
        List<AgendamentoResumoResponse> agendamentosHoje,
        List<AgendamentoResumoResponse> agendamentosProximos
) {
}
