package br.iwmvi.petshop.servico.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ServicoRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Nested
    @DisplayName("Nome")
    class Nome {

        @Test
        @DisplayName("PCE - Deve aceitar nome quando preenchido.")
        void deveAceitarNome_quandoPreenchido() {
            var request = criarServico("Banho e Tosa", "Descrição", new BigDecimal("150.00"), 60);

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar nome vazio.")
        void naoDeveAceitarNome_quandoVazio() {
            var request = criarServico("", "Descrição", new BigDecimal("150.00"), 60);

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "nome")).isTrue();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar nome com mais de 150 caracteres.")
        void naoDeveAceitarNome_quandoMaiorQue150Caracteres() {
            var nomeLongo = "a".repeat(151);
            var request = criarServico(nomeLongo, "Descrição", new BigDecimal("150.00"), 60);

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "nome")).isTrue();
        }
    }

    @Nested
    @DisplayName("Preço")
    class Preco {

        @Test
        @DisplayName("PCE - Deve aceitar preço válido.")
        void deveAceitarPreco_quandoValido() {
            var request = criarServico("Banho e Tosa", "Descrição", new BigDecimal("150.00"), 60);

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar preço nulo.")
        void naoDeveAceitarPreco_quandoNulo() {
            var request = criarServico("Banho e Tosa", "Descrição", null, 60);

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "preco")).isTrue();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar preço menor que 0.01.")
        void naoDeveAceitarPreco_quandoMenorQue001() {
            var request = criarServico("Banho e Tosa", "Descrição", new BigDecimal("0.00"), 60);

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "preco")).isTrue();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar preço maior que 9999.99.")
        void naoDeveAceitarPreco_quandoMaiorQue9999_99() {
            var request = criarServico("Banho e Tosa", "Descrição", new BigDecimal("10000.00"), 60);

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "preco")).isTrue();
        }
    }

    @Nested
    @DisplayName("Tempo Estimado")
    class TempoEstimado {

        @Test
        @DisplayName("PCE - Deve aceitar tempo estimado positivo.")
        void deveAceitarTempoEstimado_quandoPositivo() {
            var request = criarServico("Banho e Tosa", "Descrição", new BigDecimal("150.00"), 90);

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Deve aceitar tempo estimado zero.")
        void deveAceitarTempoEstimado_quandoZero() {
            var request = criarServico("Banho e Tosa", "Descrição", new BigDecimal("150.00"), 0);

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Deve aceitar tempo estimado nulo.")
        void deveAceitarTempoEstimado_quandoNulo() {
            var request = criarServico("Banho e Tosa", "Descrição", new BigDecimal("150.00"), null);

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar tempo estimado negativo.")
        void naoDeveAceitarTempoEstimado_quandoNegativo() {
            var request = criarServico("Banho e Tosa", "Descrição", new BigDecimal("150.00"), -1);

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "tempoEstimadoMinutos")).isTrue();
        }
    }

    @Nested
    @DisplayName("Descrição")
    class Descricao {

        @Test
        @DisplayName("PCE - Deve aceitar descrição quando preenchida.")
        void deveAceitarDescricao_quandoPreenchida() {
            var request = criarServico("Banho e Tosa", "Banho completo com tosa", new BigDecimal("150.00"), 60);

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Deve aceitar descrição nula.")
        void deveAceitarDescricao_quandoNula() {
            var request = criarServico("Banho e Tosa", null, new BigDecimal("150.00"), 60);

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar descrição com mais de 500 caracteres.")
        void naoDeveAceitarDescricao_quandoMaiorQue500Caracteres() {
            var descricaoLonga = "a".repeat(501);
            var request = criarServico("Banho e Tosa", descricaoLonga, new BigDecimal("150.00"), 60);

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "descricao")).isTrue();
        }
    }

    private ServicoRequest criarServico(String nome, String descricao, BigDecimal preco, Integer tempoEstimado) {
        return new ServicoRequest(nome, descricao, preco, tempoEstimado);
    }

    private boolean possuiViolacao(Set<ConstraintViolation<ServicoRequest>> violacoes, String atributo) {
        return violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath()
                .toString().equals(atributo));
    }
}
