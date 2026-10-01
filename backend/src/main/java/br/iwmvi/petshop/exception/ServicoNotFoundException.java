package br.iwmvi.petshop.exception;

public class ServicoNotFoundException extends RuntimeException {
    public ServicoNotFoundException(Long id) {
        super("Serviço com ID: " + id + " não foi encontrado.");
    }
}
