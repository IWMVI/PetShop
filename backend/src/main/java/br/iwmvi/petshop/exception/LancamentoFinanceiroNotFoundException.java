package br.iwmvi.petshop.exception;

public class LancamentoFinanceiroNotFoundException extends RuntimeException {
    public LancamentoFinanceiroNotFoundException(Long id) {
        super("Lançamento financeiro com ID: " + id + " não foi encontrado.");
    }
}
