package br.iwmvi.petshop.servico.mapper;

import br.iwmvi.petshop.servico.ServicoTestData;
import br.iwmvi.petshop.servico.dto.request.ServicoRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ServicoMapperTest {

    @Nested
    @DisplayName("Conversão para entidade")
    class ConversaoParaEntidade {

        @Test
        @DisplayName("PCE - Deve mapear todos os campos do serviço.")
        void deveMapearTodosOsCampos() {
            var request = new ServicoRequest(
                    "Banho e Tosa",
                    "Banho completo com tosa",
                    new BigDecimal("150.00"),
                    60
            );

            var servico = ServicoMapper.toEntity(request);

            assertThat(servico.getNome()).isEqualTo("Banho e Tosa");
            assertThat(servico.getDescricao()).isEqualTo("Banho completo com tosa");
            assertThat(servico.getPreco()).isEqualTo(new BigDecimal("150.00"));
            assertThat(servico.getTempoEstimadoMinutos()).isEqualTo(60);
        }
    }

    @Nested
    @DisplayName("Conversão para resposta")
    class ConversaoParaResposta {

        @Test
        @DisplayName("PCE - Deve mapear todos os campos do serviço para a resposta.")
        void deveMapearTodosOsCamposParaResposta() {
            var servico = ServicoTestData.criarServico();

            var response = ServicoMapper.toResponse(servico);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.nome()).isEqualTo("Banho e Tosa");
            assertThat(response.descricao()).isEqualTo("Banho completo com tosa");
            assertThat(response.preco()).isEqualTo(new BigDecimal("150.00"));
            assertThat(response.tempoEstimadoMinutos()).isEqualTo(60);
        }
    }
}
