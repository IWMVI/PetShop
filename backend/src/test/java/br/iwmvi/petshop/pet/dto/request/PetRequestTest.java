package br.iwmvi.petshop.pet.dto.request;

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

class PetRequestTest {

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
            var request = criarPet("Fluffy", "Gato", "Persa", 2, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar nome vazio.")
        void naoDeveAceitarNome_quandoVazio() {
            var request = criarPet("", "Gato", "Persa", 2, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "nome")).isTrue();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar nome em branco.")
        void naoDeveAceitarNome_quandoEmBranco() {
            var request = criarPet("   ", "Gato", "Persa", 2, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "nome")).isTrue();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar nome com mais de 150 caracteres.")
        void naoDeveAceitarNome_quandoMaiorQue150Caracteres() {
            var nomeLongo = "a".repeat(151);
            var request = criarPet(nomeLongo, "Gato", "Persa", 2, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "nome")).isTrue();
        }
    }

    @Nested
    @DisplayName("Especie")
    class Especie {

        @Test
        @DisplayName("PCE - Deve aceitar espécie quando preenchida.")
        void deveAceitarEspecie_quandoPreenchida() {
            var request = criarPet("Fluffy", "Gato", "Persa", 2, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar espécie vazia.")
        void naoDeveAceitarEspecie_quandoVazia() {
            var request = criarPet("Fluffy", "", "Persa", 2, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "especie")).isTrue();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar espécie com mais de 50 caracteres.")
        void naoDeveAceitarEspecie_quandoMaiorQue50Caracteres() {
            var especieLonga = "a".repeat(51);
            var request = criarPet("Fluffy", especieLonga, "Persa", 2, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "especie")).isTrue();
        }
    }

    @Nested
    @DisplayName("Raca")
    class Raca {

        @Test
        @DisplayName("PCE - Deve aceitar raça quando preenchida.")
        void deveAceitarRaca_quandoPreenchida() {
            var request = criarPet("Fluffy", "Gato", "Persa", 2, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Deve aceitar raça vazia.")
        void deveAceitarRaca_quandoVazia() {
            var request = criarPet("Fluffy", "Gato", "", 2, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Deve aceitar raça com exatamente 100 caracteres.")
        void deveAceitarRaca_quandoExatamente100Caracteres() {
            var racaComLimite = "a".repeat(100);
            var request = criarPet("Fluffy", "Gato", racaComLimite, 2, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar raça com mais de 100 caracteres.")
        void naoDeveAceitarRaca_quandoMaiorQue100Caracteres() {
            var racaLonga = "a".repeat(101);
            var request = criarPet("Fluffy", "Gato", racaLonga, 2, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "raca")).isTrue();
        }
    }

    @Nested
    @DisplayName("Idade")
    class Idade {

        @Test
        @DisplayName("PCE - Deve aceitar idade positiva.")
        void deveAceitarIdade_quandoPositiva() {
            var request = criarPet("Fluffy", "Gato", "Persa", 5, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Deve aceitar idade zero.")
        void deveAceitarIdade_quandoZero() {
            var request = criarPet("Fluffy", "Gato", "Persa", 0, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Deve aceitar idade nula.")
        void deveAceitarIdade_quandoNula() {
            var request = criarPet("Fluffy", "Gato", "Persa", null, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar idade negativa.")
        void naoDeveAceitarIdade_quandoNegativa() {
            var request = criarPet("Fluffy", "Gato", "Persa", -1, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "idade")).isTrue();
        }
    }

    @Nested
    @DisplayName("Peso")
    class Peso {

        @Test
        @DisplayName("PCE - Deve aceitar peso válido.")
        void deveAceitarPeso_quandoValido() {
            var request = criarPet("Fluffy", "Gato", "Persa", 2, new BigDecimal("5.50"));

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Deve aceitar peso nulo.")
        void deveAceitarPeso_quandoNulo() {
            var request = criarPet("Fluffy", "Gato", "Persa", 2, null);

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar peso menor que 0.01.")
        void naoDeveAceitarPeso_quandoMenorQue001() {
            var request = criarPet("Fluffy", "Gato", "Persa", 2, new BigDecimal("0.00"));

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "peso")).isTrue();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar peso maior que 999.99.")
        void naoDeveAceitarPeso_quandoMaiorQue99999() {
            var request = criarPet("Fluffy", "Gato", "Persa", 2, new BigDecimal("1000.00"));

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "peso")).isTrue();
        }
    }

    private PetRequest criarPet(String nome, String especie, String raca, Integer idade, BigDecimal peso) {
        return new PetRequest(nome, especie, raca, idade, peso);
    }

    private boolean possuiViolacao(Set<ConstraintViolation<PetRequest>> violacoes, String atributo) {
        return violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath()
                .toString().equals(atributo));
    }
}
