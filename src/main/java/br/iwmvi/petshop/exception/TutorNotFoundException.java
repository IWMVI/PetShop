package br.iwmvi.petshop.exception;

public class TutorNotFoundException extends RuntimeException {
    public TutorNotFoundException(Long tutorId) {
        super("Tutor não encontrado" + tutorId);
    }
}
