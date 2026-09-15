package br.iwmvi.petshop.endereco.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EnderecoRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Nested
    @DisplayName("CEP")
    class Cep {

        @ParameterizedTest
        @ValueSource(strings = {"01001001", "01001-001"})
        @DisplayName("PCE - Deve aceitar CEP quando estiver em formato válido.")
        void deveAceitarCep_quandoFormatoForValido(String cep) {
            var request = criarEndereco(cep, "SP");

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar CEP vazio.")
        void naoDeveAceitarCep_quandoEstiverVazio() {
            var request = criarEndereco("", "SP");

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "cep")).isTrue();
        }


        @Test
        @DisplayName("PCE - Não deve aceitar CEP com caracteres inválidos.")
        void naoDeveAceitarCep_quandoPossuirCaracteresInvalidos() {
            var request = criarEndereco("01001-0A0", "SP");

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "cep")).isTrue();
        }

        @Test
        @DisplayName("AVL - Não deve aceitar CEP com 7 dígitos")
        void naoDeveAceitarCep_quandoPossuirSeteDigitos() {
            var request = criarEndereco("0100100", "SP");

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "cep")).isTrue();
        }

        @Test
        @DisplayName("AVL - Deve aceitar CEP com exatamente 8 dígitos")
        void deveAceitarCep_quandoPossuirOitoDigitos() {
            var request = criarEndereco("01001001", "SP");

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("AVL - Não deve aceitar CEP com 9 dígitos sem hífen")
        void naoDeveAceitarCep_quandoPossuirNoveDigitosSemHifen() {
            var request = criarEndereco("010010011", "SP");

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "cep")).isTrue();
        }
    }

    @Nested
    @DisplayName("Estado")
    class Estado {

        @Test
        @DisplayName("AVL - Não deve aceitar estado com 1 caractere")
        void naoDeveAceitarEstado_quandoPossuirUmCaractere() {
            var request = criarEndereco("01001001", "S");

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "estado")).isTrue();
        }

        @Test
        @DisplayName("AVL - Deve aceitar estado com exatamente 2 letras")
        void deveAceitarEstado_quandoPossuirDuasLetras() {
            var request = criarEndereco("01001001", "SP");

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("AVL - Não deve aceitar estado com 3 caracteres")
        void naoDeveAceitarEstado_quandoPossuirTresCaracteres() {
            var request = criarEndereco("01001001", "SPA");

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "estado")).isTrue();
        }
    }

    private EnderecoRequest criarEndereco(String cep, String estado) {
        return new EnderecoRequest(
                cep,
                "Praça da Sé",
                "100",
                null,
                "Sé",
                "São Paulo",
                estado
        );
    }

    private boolean possuiViolacao(Set<ConstraintViolation<EnderecoRequest>> violacoes, String atributo) {
        return violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath()
                .toString().equals(atributo));
    }
}
