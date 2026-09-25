package br.iwmvi.petshop.historico.dto.request;

import br.iwmvi.petshop.historico.model.TipoEvento;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HistoricoPetRequestTest {

    private static final LocalDateTime ONTEM = LocalDateTime.now().minusDays(1);

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("PCE - Deve aceitar evento com todos os campos válidos.")
    void deveAceitarEvento_quandoTodosOsCamposValidos() {
        var request = criarRequest(TipoEvento.CONSULTA, "Consulta de rotina", ONTEM, 1L);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("PCE - Deve aceitar evento sem funcionário.")
    void deveAceitarEvento_quandoSemFuncionario() {
        var request = criarRequest(TipoEvento.OUTRO, "Vacina aplicada em outra clínica", ONTEM, null);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Nested
    @DisplayName("Tipo do evento")
    class Tipo {

        @Test
        @DisplayName("PCE - Não deve aceitar tipo de evento nulo.")
        void naoDeveAceitarTipo_quandoNulo() {
            var request = criarRequest(null, "Consulta de rotina", ONTEM, 1L);

            assertThat(possuiViolacao(validator.validate(request), "tipoEvento")).isTrue();
        }
    }

    @Nested
    @DisplayName("Descrição")
    class Descricao {

        @Test
        @DisplayName("PCE - Não deve aceitar descrição vazia.")
        void naoDeveAceitarDescricao_quandoVazia() {
            var request = criarRequest(TipoEvento.CONSULTA, "", ONTEM, 1L);

            assertThat(possuiViolacao(validator.validate(request), "descricao")).isTrue();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar descrição em branco.")
        void naoDeveAceitarDescricao_quandoEmBranco() {
            var request = criarRequest(TipoEvento.CONSULTA, "   ", ONTEM, 1L);

            assertThat(possuiViolacao(validator.validate(request), "descricao")).isTrue();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar descrição nula.")
        void naoDeveAceitarDescricao_quandoNula() {
            var request = criarRequest(TipoEvento.CONSULTA, null, ONTEM, 1L);

            assertThat(possuiViolacao(validator.validate(request), "descricao")).isTrue();
        }
    }

    @Nested
    @DisplayName("Data do evento")
    class DataEvento {

        @Test
        @DisplayName("PCE - Não deve aceitar data do evento nula.")
        void naoDeveAceitarData_quandoNula() {
            var request = criarRequest(TipoEvento.CONSULTA, "Consulta de rotina", null, 1L);

            assertThat(possuiViolacao(validator.validate(request), "dataEvento")).isTrue();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar data do evento no futuro.")
        void naoDeveAceitarData_quandoNoFuturo() {
            var amanha = LocalDateTime.now().plusDays(1);
            var request = criarRequest(TipoEvento.CONSULTA, "Consulta de rotina", amanha, 1L);

            assertThat(possuiViolacao(validator.validate(request), "dataEvento")).isTrue();
        }

        @Test
        @DisplayName("PCE - Deve aceitar data do evento no momento atual.")
        void deveAceitarData_quandoAgora() {
            var agora = LocalDateTime.now().minusSeconds(1);
            var request = criarRequest(TipoEvento.CONSULTA, "Consulta de rotina", agora, 1L);

            assertThat(validator.validate(request)).isEmpty();
        }
    }

    private HistoricoPetRequest criarRequest(TipoEvento tipo, String descricao,
                                             LocalDateTime dataEvento, Long funcionarioId) {
        return new HistoricoPetRequest(tipo, descricao, dataEvento, funcionarioId);
    }

    private boolean possuiViolacao(Set<ConstraintViolation<HistoricoPetRequest>> violacoes, String atributo) {
        return violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath()
                .toString().equals(atributo));
    }
}
