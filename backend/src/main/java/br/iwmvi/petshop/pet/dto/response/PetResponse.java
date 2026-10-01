package br.iwmvi.petshop.pet.dto.response;

import java.math.BigDecimal;

public record PetResponse(
        Long id,
        String nome,
        String especie,
        BigDecimal peso,
        String raca,
        Integer idade
) {
}
