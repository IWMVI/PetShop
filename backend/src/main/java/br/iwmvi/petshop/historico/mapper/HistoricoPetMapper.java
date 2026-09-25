package br.iwmvi.petshop.historico.mapper;

import br.iwmvi.petshop.common.mapper.ResponseMapper;
import br.iwmvi.petshop.funcionario.model.Funcionario;
import br.iwmvi.petshop.historico.dto.request.HistoricoPetRequest;
import br.iwmvi.petshop.historico.dto.response.HistoricoPetResponse;
import br.iwmvi.petshop.historico.model.HistoricoPet;
import br.iwmvi.petshop.pet.model.Pet;
import org.springframework.stereotype.Component;

@Component
public class HistoricoPetMapper implements ResponseMapper<HistoricoPet, HistoricoPetResponse> {

    public HistoricoPet toEntity(HistoricoPetRequest request, Pet pet, Funcionario funcionario) {
        return new HistoricoPet(
                pet,
                funcionario,
                request.tipoEvento(),
                request.descricao(),
                request.dataEvento()
        );
    }

    @Override
    public HistoricoPetResponse toResponse(HistoricoPet historico) {
        var funcionario = historico.getFuncionario();

        return new HistoricoPetResponse(
                historico.getId(),
                historico.getPet().getId(),
                historico.getTipoEvento(),
                historico.getDescricao(),
                historico.getDataEvento(),
                funcionario != null ? funcionario.getId() : null,
                funcionario != null ? funcionario.getNome() : null,
                historico.getCreatedAt()
        );
    }
}
