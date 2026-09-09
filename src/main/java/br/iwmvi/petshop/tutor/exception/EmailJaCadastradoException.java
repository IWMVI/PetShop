package br.iwmvi.petshop.tutor.exception;

public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException(String email) {
        super("Email ja cadastrado: " + email);
    }
}
