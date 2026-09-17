package br.iwmvi.petshop.exception;

public class PetNotFoundException extends RuntimeException {
    public PetNotFoundException(Long id) {
        super("Pet com ID: " + id + " não foi encontrado.");
    }
}
