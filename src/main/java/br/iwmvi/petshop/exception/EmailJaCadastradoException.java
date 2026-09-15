package br.iwmvi.petshop.exception;

public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException(String email) {
        super("Email ja cadastrado: " + email);
    }
}
