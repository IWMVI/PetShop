package br.iwmvi.petshop.tutor.dto.request;

import br.iwmvi.petshop.endereco.dto.request.EnderecoRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TutorRequestTest {

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
            var request = criarTutor("Wallace", "wallace@test.com", "11999999999", criarEndereco());

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar nome vazio.")
        void naoDeveAceitarNome_quandoVazio() {
            var request = criarTutor("", "wallace@test.com", "11999999999", criarEndereco());

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "nome")).isTrue();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar nome em branco.")
        void naoDeveAceitarNome_quandoEmBranco() {
            var request = criarTutor("   ", "wallace@test.com", "11999999999", criarEndereco());

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "nome")).isTrue();
        }
    }

    @Nested
    @DisplayName("E-mail")
    class Email {

        @Test
        @DisplayName("PCE - Deve aceitar e-mail válido.")
        void deveAceitarEmail_quandoValido() {
            var request = criarTutor("Wallace", "wallace@test.com", "11999999999", criarEndereco());

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar e-mail vazio.")
        void naoDeveAceitarEmail_quandoVazio() {
            var request = criarTutor("Wallace", "", "11999999999", criarEndereco());

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "email")).isTrue();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar e-mail inválido.")
        void naoDeveAceitarEmail_quandoInvalido() {
            var request = criarTutor("Wallace", "invalido", "11999999999", criarEndereco());

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "email")).isTrue();
        }
    }

    @Nested
    @DisplayName("Telefone")
    class Telefone {

        @Test
        @DisplayName("PCE - Deve aceitar telefone com 10 dígitos.")
        void deveAceitarTelefone_quandoPossuirDezDigitos() {
            var request = criarTutor("Wallace", "wallace@test.com", "1199999999", criarEndereco());

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Deve aceitar telefone com 11 dígitos.")
        void deveAceitarTelefone_quandoPossuirOnzeDigitos() {
            var request = criarTutor("Wallace", "wallace@test.com", "11999999999", criarEndereco());

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar telefone vazio.")
        void naoDeveAceitarTelefone_quandoVazio() {
            var request = criarTutor("Wallace", "wallace@test.com", "", criarEndereco());

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "telefone")).isTrue();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar telefone com letras.")
        void naoDeveAceitarTelefone_quandoPossuirLetras() {
            var request = criarTutor("Wallace", "wallace@test.com", "telefone", criarEndereco());

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "telefone")).isTrue();
        }

        @Test
        @DisplayName("AVL - Não deve aceitar telefone com 9 dígitos.")
        void naoDeveAceitarTelefone_quandoPossuirNoveDigitos() {
            var request = criarTutor("Wallace", "wallace@test.com", "119999999", criarEndereco());

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "telefone")).isTrue();
        }
    }

    @Nested
    @DisplayName("Endereço")
    class Endereco {

        @Test
        @DisplayName("PCE - Deve aceitar tutor quando o endereço for válido.")
        void deveAceitarEndereco_quandoValido() {
            var request = criarTutor("Wallace", "wallace@test.com", "11999999999", criarEndereco());

            var violacoes = validator.validate(request);

            assertThat(violacoes).isEmpty();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar tutor com endereço nulo.")
        void naoDeveAceitarEndereco_quandoNulo() {
            var request = criarTutor("Wallace", "wallace@test.com", "11999999999", null);

            var violacoes = validator.validate(request);

            assertThat(possuiViolacao(violacoes, "endereco")).isTrue();
        }

        @Test
        @DisplayName("PCE - Não deve aceitar endereço com CEP inválido.")
        void naoDeveAceitarEndereco_quandoCepInvalido() {
            var endereco = new EnderecoRequest(
                    "123", "Praça da Sé", "100", null, "Sé", "São Paulo", "SP"
            );

            var request = criarTutor("Wallace", "wallace@test.com", "11999999999", endereco);

            var violacoes = validator.validate(request);

            assertThat(violacoes).isNotEmpty();
        }
    }

    private TutorRequest criarTutor(String nome, String email, String telefone, EnderecoRequest endereco) {
        return new TutorRequest(nome, email, telefone, endereco);
    }

    private EnderecoRequest criarEndereco() {
        return new EnderecoRequest(
                "01001-001",
                "Praça da Sé",
                "100",
                null,
                "Sé",
                "São Paulo",
                "SP"
        );
    }

    private boolean possuiViolacao(Set<ConstraintViolation<TutorRequest>> violacoes, String atributo) {
        return violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath()
                .toString().equals(atributo));
    }
}