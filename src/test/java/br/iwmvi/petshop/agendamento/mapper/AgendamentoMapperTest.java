package br.iwmvi.petshop.agendamento.mapper;

import br.iwmvi.petshop.agendamento.AgendamentoTestData;
import br.iwmvi.petshop.agendamento.model.AgendamentoStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgendamentoMapperTest {

    @Test
    @DisplayName("PCE - Deve mapear request para entidade com valor total e serviços")
    void deveMapearRequestParaEntidade() {
        var pet = AgendamentoTestData.criarPet();
        var request = AgendamentoTestData.criarAgendamentoRequest(LocalDateTime.now().plusDays(1));
        var servicos = List.of(
                AgendamentoTestData.criarServico(10L, "Banho", "50.00"),
                AgendamentoTestData.criarServico(11L, "Tosa", "30.00")
        );

        var entidade = AgendamentoMapper.toEntity(request, pet, servicos);

        assertThat(entidade.getPet().getId()).isEqualTo(2L);
        assertThat(entidade.getStatus()).isEqualTo(AgendamentoStatus.AGENDADO);
        assertThat(entidade.getValorTotal()).isEqualTo(new BigDecimal("80.00"));
        assertThat(entidade.getAgendamentoServicos()).hasSize(2);
    }

    @Test
    @DisplayName("PCE - Deve mapear entidade para response")
    void deveMapearEntidadeParaResponse() {
        var pet = AgendamentoTestData.criarPet();
        var servicos = List.of(
                AgendamentoTestData.criarServico(10L, "Banho", "50.00"),
                AgendamentoTestData.criarServico(11L, "Tosa", "30.00")
        );
        var agendamento = AgendamentoTestData.criarAgendamento(pet, LocalDateTime.now().plusDays(1), servicos);

        var response = AgendamentoMapper.toResponse(agendamento);

        assertThat(response.id()).isEqualTo(50L);
        assertThat(response.petId()).isEqualTo(2L);
        assertThat(response.status()).isEqualTo(AgendamentoStatus.AGENDADO);
        assertThat(response.valorTotal()).isEqualTo(new BigDecimal("80.00"));
        assertThat(response.servicos()).hasSize(2);
    }

    @Test
    @DisplayName("PCE - Deve usar id do relacionamento ao montar serviços da response")
    void deveUsarIdDoRelacionamentoNaResponse() {
        var pet = AgendamentoTestData.criarPet();
        var servico = AgendamentoTestData.criarServico(10L, "Banho", "50.00");
        var agendamento = AgendamentoTestData.criarAgendamento(pet, LocalDateTime.now().plusDays(1), List.of(servico));

        var response = AgendamentoMapper.toResponse(agendamento);

        assertThat(response.servicos().getFirst().servicoId()).isEqualTo(10L);
        assertThat(response.servicos().getFirst().precoCobrado()).isEqualTo(new BigDecimal("50.00"));
    }
}
