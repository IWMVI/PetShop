package br.iwmvi.petshop.funcionario.dto.request;

import br.iwmvi.petshop.funcionario.model.Cargo;
import br.iwmvi.petshop.tutor.CpfTestData;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FuncionarioRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("PCE - Deve aceitar funcionário com todos os campos válidos.")
    void deveAceitarFuncionario_quandoCamposValidos() {
        var request = criarRequest("Dra. Ana", CpfTestData.VALIDO, Cargo.VETERINARIO, "11988887777");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("PCE - Não deve aceitar nome vazio.")
    void naoDeveAceitarNome_quandoVazio() {
        var request = criarRequest("", CpfTestData.VALIDO, Cargo.VETERINARIO, "11988887777");

        assertThat(possuiViolacao(validator.validate(request), "nome")).isTrue();
    }

    @Test
    @DisplayName("PCE - Não deve aceitar CPF inválido.")
    void naoDeveAceitarCpf_quandoInvalido() {
        var request = criarRequest("Dra. Ana", "11111111111", Cargo.VETERINARIO, "11988887777");

        assertThat(possuiViolacao(validator.validate(request), "cpf")).isTrue();
    }

    @Test
    @DisplayName("PCE - Não deve aceitar cargo nulo.")
    void naoDeveAceitarCargo_quandoNulo() {
        var request = criarRequest("Dra. Ana", CpfTestData.VALIDO, null, "11988887777");

        assertThat(possuiViolacao(validator.validate(request), "cargo")).isTrue();
    }

    @Test
    @DisplayName("PCE - Não deve aceitar telefone com menos de 10 dígitos.")
    void naoDeveAceitarTelefone_quandoMenosDe10Digitos() {
        var request = criarRequest("Dra. Ana", CpfTestData.VALIDO, Cargo.VETERINARIO, "119888877");

        assertThat(possuiViolacao(validator.validate(request), "telefone")).isTrue();
    }

    private FuncionarioRequest criarRequest(String nome, String cpf, Cargo cargo, String telefone) {
        return new FuncionarioRequest(nome, cpf, cargo, telefone);
    }

    private boolean possuiViolacao(Set<ConstraintViolation<FuncionarioRequest>> violacoes, String atributo) {
        return violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath()
                .toString().equals(atributo));
    }
}
