package br.iwmvi.petshop.exception;

public class PagamentoNotFoundException extends RuntimeException {
    public PagamentoNotFoundException(Long id) {
        super("Pagamento com ID: " + id + " não foi encontrado.");
    }
}
