package br.iwmvi.petshop.dashboard.service;

import br.iwmvi.petshop.agendamento.dto.response.AgendamentoResumoResponse;
import br.iwmvi.petshop.agendamento.repository.AgendamentoRepository;
import br.iwmvi.petshop.dashboard.dto.response.DashboardGeralResponse;
import br.iwmvi.petshop.financeiro.dto.response.DashboardResponse;
import br.iwmvi.petshop.financeiro.service.LancamentoFinanceiroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private LancamentoFinanceiroService financeiroService;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(financeiroService, agendamentoRepository);
    }

    @Nested
    @DisplayName("Montagem do dashboard geral")
    class Montagem {

        @Test
        @DisplayName("Deve retornar o resumo financeiro de LancamentoFinanceiroService sem alterá-lo")
        void deveRetornarResumoFinanceiroInalterado() {
            DashboardResponse financeiro = new DashboardResponse(
                    new BigDecimal("100.00"), new BigDecimal("50.00"),
                    new BigDecimal("10.00"), new BigDecimal("5.00"),
                    new BigDecimal("80.00"), new BigDecimal("90.00"),
                    List.of()
            );
            when(financeiroService.dashboard()).thenReturn(financeiro);
            when(agendamentoRepository.buscarAgendadosNoPeriodo(any(), any(), any())).thenReturn(List.of());

            DashboardGeralResponse response = dashboardService.montar();

            assertThat(response.financeiro()).isSameAs(financeiro);
            verify(financeiroService).dashboard();
            verifyNoMoreInteractions(financeiroService);
        }

        @Test
        @DisplayName("Deve buscar agendamentos de hoje com o intervalo exato do dia atual")
        void deveBuscarAgendamentosDeHojeComIntervaloDoDiaAtual() {
            mockFinanceiro();
            AgendamentoResumoResponse agendamentoHoje = criarAgendamentoResumo(1L);
            when(agendamentoRepository.buscarAgendadosNoPeriodo(any(), any(), any(Pageable.class)))
                    .thenReturn(List.of(agendamentoHoje))
                    .thenReturn(List.of());

            DashboardGeralResponse response = dashboardService.montar();

            ArgumentCaptor<LocalDateTime> inicioCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
            ArgumentCaptor<LocalDateTime> fimCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(agendamentoRepository, times(2))
                    .buscarAgendadosNoPeriodo(inicioCaptor.capture(), fimCaptor.capture(), pageableCaptor.capture());

            LocalDate hoje = LocalDate.now();
            assertThat(inicioCaptor.getAllValues().get(0)).isEqualTo(hoje.atStartOfDay());
            assertThat(fimCaptor.getAllValues().get(0)).isEqualTo(hoje.atTime(LocalTime.MAX));
            assertThat(pageableCaptor.getAllValues().get(0).isUnpaged()).isTrue();
            assertThat(response.agendamentosHoje()).containsExactly(agendamentoHoje);
        }

        @Test
        @DisplayName("Deve buscar os próximos agendamentos com início após o fim de hoje e no máximo 5 itens")
        void deveBuscarProximosAgendamentosComLimiteDeCinco() {
            mockFinanceiro();
            AgendamentoResumoResponse agendamentoProximo = criarAgendamentoResumo(2L);
            when(agendamentoRepository.buscarAgendadosNoPeriodo(any(), any(), any(Pageable.class)))
                    .thenReturn(List.of())
                    .thenReturn(List.of(agendamentoProximo));

            DashboardGeralResponse response = dashboardService.montar();

            ArgumentCaptor<LocalDateTime> inicioCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
            ArgumentCaptor<LocalDateTime> fimCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(agendamentoRepository, times(2))
                    .buscarAgendadosNoPeriodo(inicioCaptor.capture(), fimCaptor.capture(), pageableCaptor.capture());

            LocalDate hoje = LocalDate.now();
            LocalDateTime fimHoje = hoje.atTime(LocalTime.MAX);
            LocalDateTime inicioProximos = inicioCaptor.getAllValues().get(1);
            LocalDateTime fimProximos = fimCaptor.getAllValues().get(1);

            assertThat(inicioProximos).isAfter(fimHoje);
            assertThat(fimProximos).isEqualTo(hoje.plusDays(30).atTime(LocalTime.MAX));

            Pageable pageableProximos = pageableCaptor.getAllValues().get(1);
            assertThat(pageableProximos.isPaged()).isTrue();
            assertThat(pageableProximos.getPageSize()).isEqualTo(5);
            assertThat(pageableProximos.getPageNumber()).isZero();

            assertThat(response.agendamentosProximos()).containsExactly(agendamentoProximo);
        }
    }

    private void mockFinanceiro() {
        when(financeiroService.dashboard()).thenReturn(new DashboardResponse(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, List.of()
        ));
    }

    private AgendamentoResumoResponse criarAgendamentoResumo(Long id) {
        return new AgendamentoResumoResponse(id, LocalDateTime.now(), 1L, "Rex", 1L, "Wallace", new BigDecimal("50.00"));
    }
}
