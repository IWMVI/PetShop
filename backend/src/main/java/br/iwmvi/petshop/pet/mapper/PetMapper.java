package br.iwmvi.petshop.pet.mapper;

import br.iwmvi.petshop.common.mapper.ResponseMapper;
import br.iwmvi.petshop.pet.dto.request.PetRequest;
import br.iwmvi.petshop.pet.dto.response.PetResponse;
import br.iwmvi.petshop.pet.model.Pet;
import br.iwmvi.petshop.tutor.model.Tutor;
import org.springframework.stereotype.Component;

@Component
public class PetMapper implements ResponseMapper<Pet, PetResponse> {

    public Pet toEntity(PetRequest request, Tutor tutor) {
        return new Pet(
                request.nome(),
                request.especie(),
                request.peso(),
                request.raca(),
                request.idade(),
                tutor
        );
    }

    @Override
    public PetResponse toResponse(Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getNome(),
                pet.getEspecie(),
                pet.getPeso(),
                pet.getRaca(),
                pet.getIdade()
        );
    }
}