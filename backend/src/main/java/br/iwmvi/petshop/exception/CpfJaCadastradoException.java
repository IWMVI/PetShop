package br.iwmvi.petshop.exception;

public class CpfJaCadastradoException extends RuntimeException {
    public CpfJaCadastradoException() {
        super("CPF já cadastrado.");
    }
}
