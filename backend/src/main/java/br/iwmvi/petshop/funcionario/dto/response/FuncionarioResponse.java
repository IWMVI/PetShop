package br.iwmvi.petshop.funcionario.dto.response;

import br.iwmvi.petshop.funcionario.model.Cargo;

public record FuncionarioResponse(
        Long id,
        String nome,
        String cpf,
        Cargo cargo,
        String telefone
) {
}
