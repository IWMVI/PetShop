package br.iwmvi.petshop.agendamento.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AgendamentoRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("PCE - Deve aceitar request válido")
    void deveAceitarRequestValido() {
        var request = criarRequest(LocalDateTime.now().plusHours(2), "Observação", List.of(1L));

        var violacoes = validator.validate(request);

        assertThat(violacoes).isEmpty();
    }

    @Test
    @DisplayName("ESE - Não deve aceitar data/hora no passado")
    void naoDeveAceitarDataHoraNoPassado() {
        var request = criarRequest(LocalDateTime.now().minusMinutes(1), "Observação", List.of(1L));

        var violacoes = validator.validate(request);

        assertThat(possuiViolacao(violacoes, "dataHora")).isTrue();
    }

    @Test
    @DisplayName("ESE - Não deve aceitar data/hora nula")
    void naoDeveAceitarDataHoraNula() {
        var request = criarRequest(null, "Observação", List.of(1L));

        var violacoes = validator.validate(request);

        assertThat(possuiViolacao(violacoes, "dataHora")).isTrue();
    }

    @Test
    @DisplayName("ESE - Não deve aceitar lista de serviços vazia")
    void naoDeveAceitarListaServicosVazia() {
        var request = criarRequest(LocalDateTime.now().plusHours(2), "Observação", List.of());

        var violacoes = validator.validate(request);

        assertThat(possuiViolacao(violacoes, "servicoIds")).isTrue();
    }

    @Test
    @DisplayName("ESE - Não deve aceitar id de serviço não positivo")
    void naoDeveAceitarIdServicoNaoPositivo() {
        var request = criarRequest(LocalDateTime.now().plusHours(2), "Observação", List.of(0L));

        var violacoes = validator.validate(request);

        assertThat(possuiViolacao(violacoes, "servicoIds[0].<list element>")).isTrue();
    }

    @Test
    @DisplayName("ESE - Não deve aceitar observação maior que 1000 caracteres")
    void naoDeveAceitarObservacaoMaiorQueMilCaracteres() {
        var request = criarRequest(LocalDateTime.now().plusHours(2), "a".repeat(1001), List.of(1L));

        var violacoes = validator.validate(request);

        assertThat(possuiViolacao(violacoes, "observacoes")).isTrue();
    }

    private AgendamentoRequest criarRequest(LocalDateTime dataHora, String observacoes, List<Long> servicoIds) {
        return new AgendamentoRequest(dataHora, observacoes, servicoIds);
    }

    private boolean possuiViolacao(Set<ConstraintViolation<AgendamentoRequest>> violacoes, String atributo) {
        return violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath().toString().equals(atributo));
    }
}
