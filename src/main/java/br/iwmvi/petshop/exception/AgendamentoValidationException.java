package br.iwmvi.petshop.exception;

public class AgendamentoValidationException extends RuntimeException {
    public AgendamentoValidationException(String mensagem) {
        super(mensagem);
    }
}
