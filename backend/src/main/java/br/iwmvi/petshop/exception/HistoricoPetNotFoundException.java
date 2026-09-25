package br.iwmvi.petshop.exception;

public class HistoricoPetNotFoundException extends RuntimeException {
    public HistoricoPetNotFoundException(Long id) {
        super("Evento do histórico com ID: " + id + " não foi encontrado.");
    }
}
