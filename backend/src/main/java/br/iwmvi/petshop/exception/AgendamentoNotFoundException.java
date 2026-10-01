package br.iwmvi.petshop.exception;

public class AgendamentoNotFoundException extends RuntimeException {
    public AgendamentoNotFoundException(Long id) {
        super("Agendamento com ID: " + id + " não foi encontrado.");
    }
}
