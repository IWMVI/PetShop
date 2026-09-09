package br.iwmvi.petshop.tutor.mapper;

import br.iwmvi.petshop.tutor.dto.request.EnderecoRequest;
import br.iwmvi.petshop.tutor.dto.response.EnderecoResponse;
import br.iwmvi.petshop.tutor.model.Endereco;

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
