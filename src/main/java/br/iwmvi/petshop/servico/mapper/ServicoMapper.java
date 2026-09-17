package br.iwmvi.petshop.servico.mapper;

import br.iwmvi.petshop.servico.dto.request.ServicoRequest;
import br.iwmvi.petshop.servico.dto.response.ServicoResponse;
import br.iwmvi.petshop.servico.model.Servico;

public final class ServicoMapper {

    private ServicoMapper() {
    }

    public static Servico toEntity(ServicoRequest request) {
        return new Servico(
                request.nome(),
                request.descricao(),
                request.preco(),
                request.tempoEstimadoMinutos()
        );
    }

    public static ServicoResponse toResponse(Servico servico) {
        return new ServicoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getPreco(),
                servico.getTempoEstimadoMinutos()
        );
    }
}
