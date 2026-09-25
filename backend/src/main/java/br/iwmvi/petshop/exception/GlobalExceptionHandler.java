package br.iwmvi.petshop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TutorNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleTutorNotFound(TutorNotFoundException ex) {
        return Map.of("mensagem", ex.getMessage());
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleEmailJaCadastrado(EmailJaCadastradoException ex) {
        return Map.of("mensagem", ex.getMessage());
    }

    @ExceptionHandler(CpfJaCadastradoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleCpfJaCadastrado(CpfJaCadastradoException ex) {
        return Map.of("mensagem", ex.getMessage());
    }

    @ExceptionHandler(PetNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handlePetNotFound(PetNotFoundException ex) {
        return Map.of("mensagem", ex.getMessage());
    }

    @ExceptionHandler(HistoricoPetNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleHistoricoPetNotFound(HistoricoPetNotFoundException ex) {
        return Map.of("mensagem", ex.getMessage());
    }

    @ExceptionHandler(ServicoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleServicoNotFound(ServicoNotFoundException ex) {
        return Map.of("mensagem", ex.getMessage());
    }

    @ExceptionHandler(AgendamentoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleAgendamentoNotFound(AgendamentoNotFoundException ex) {
        return Map.of("mensagem", ex.getMessage());
    }

    @ExceptionHandler(AgendamentoValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleAgendamentoValidation(AgendamentoValidationException ex) {
        return Map.of("mensagem", ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleEntityNotFound(EntityNotFoundException ex) {
        return Map.of("mensagem", ex.getMessage());
    }
}