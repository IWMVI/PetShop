package br.iwmvi.petshop.tutor.dto.response;

import br.iwmvi.petshop.endereco.dto.response.EnderecoResponse;

public record TutorResponse(
        Long id,
        String nome,
        String cpf,
        String email,
        String telefone,
        EnderecoResponse endereco
) {
}
