package br.iwmvi.petshop.endereco.mapper;

import br.iwmvi.petshop.endereco.dto.request.EnderecoRequest;
import br.iwmvi.petshop.endereco.dto.response.EnderecoResponse;
import br.iwmvi.petshop.endereco.model.Endereco;

public final class EnderecoMapper {

    private EnderecoMapper() {
    }

    public static Endereco toEntity(EnderecoRequest request) {
        return new Endereco(
                request.cep().replaceAll("\\D", ""),
                request.logradouro(),
                request.numero(),
                request.complemento(),
                request.bairro(),
                request.cidade(),
                request.estado().toUpperCase()
        );
    }

    public static EnderecoResponse toResponse(Endereco endereco) {
        return new EnderecoResponse(
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado()
        );
    }
}
