package br.iwmvi.petshop.dashboard.service;

import br.iwmvi.petshop.agendamento.dto.response.AgendamentoResumoResponse;
import br.iwmvi.petshop.agendamento.repository.AgendamentoRepository;
import br.iwmvi.petshop.dashboard.dto.response.DashboardGeralResponse;
import br.iwmvi.petshop.financeiro.service.LancamentoFinanceiroService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Agrega os dados da página inicial do sistema: resumo financeiro (reaproveitado de
 * {@link LancamentoFinanceiroService}) e agendamentos de hoje/próximos.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int LIMITE_PROXIMOS = 5;

    private final LancamentoFinanceiroService financeiroService;
    private final AgendamentoRepository agendamentoRepository;

    @Transactional(readOnly = true)
    public DashboardGeralResponse montar() {
        var financeiro = financeiroService.dashboard();

        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioHoje = hoje.atStartOfDay();
        LocalDateTime fimHoje = hoje.atTime(LocalTime.MAX);

        List<AgendamentoResumoResponse> agendamentosHoje =
                agendamentoRepository.buscarAgendadosNoPeriodo(inicioHoje, fimHoje, Pageable.unpaged());

        LocalDateTime fimJanelaProximos = hoje.plusDays(30).atTime(LocalTime.MAX);
        List<AgendamentoResumoResponse> agendamentosProximos = agendamentoRepository.buscarAgendadosNoPeriodo(
                fimHoje.plusNanos(1), fimJanelaProximos, PageRequest.of(0, LIMITE_PROXIMOS));

        return new DashboardGeralResponse(financeiro, agendamentosHoje, agendamentosProximos);
    }
}
