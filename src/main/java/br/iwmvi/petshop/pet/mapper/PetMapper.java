package br.iwmvi.petshop.pet.mapper;

import br.iwmvi.petshop.pet.dto.request.PetRequest;
import br.iwmvi.petshop.pet.dto.response.PetResponse;
import br.iwmvi.petshop.pet.model.Pet;
import br.iwmvi.petshop.tutor.model.Tutor;

public final class PetMapper {

    private PetMapper() {
    }

    public static Pet toEntity(PetRequest request, Tutor tutor) {
        return new Pet(
                request.nome(),
                request.especie(),
                request.peso(),
                request.raca(),
                request.idade(),
                tutor
        );
    }

    public static PetResponse toResponse(Pet response) {
        return new PetResponse(
                response.getId(),
                response.getNome(),
                response.getEspecie(),
                response.getPeso(),
                response.getRaca(),
                response.getIdade()
        );
    }
}
