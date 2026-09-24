package br.iwmvi.petshop.pet;

import br.iwmvi.petshop.pet.dto.request.PetRequest;
import br.iwmvi.petshop.pet.model.Pet;
import br.iwmvi.petshop.tutor.model.Tutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

public class PetTestData {

    public static PetRequest criarPetRequest() {
        return new PetRequest(
                "Fluffy",
                "Gato",
                "Persa",
                2,
                new BigDecimal("5.50")
        );
    }

    public static Pet criarPet(Tutor tutor) {
        var pet = new Pet(
                "Fluffy",
                "Gato",
                new BigDecimal("5.50"),
                "Persa",
                2,
                tutor
        );

        ReflectionTestUtils.setField(pet, "id", 1L);

        return pet;
    }
}
