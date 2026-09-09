package br.iwmvi.petshop.tutor.dto.response;

public record TutorResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        EnderecoResponse endereco
) {
}
