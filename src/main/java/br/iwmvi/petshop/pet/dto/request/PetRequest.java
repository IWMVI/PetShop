package br.iwmvi.petshop.pet.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PetRequest(

        @NotBlank
        @Size(max = 150)
        String nome,

        @NotBlank
        @Size(max = 50)
        String especie,

        @Size(max = 50)
        String raca,

        @PositiveOrZero
        Integer idade,

        @DecimalMin("0.01")
        @DecimalMax("999.99")
        BigDecimal peso
) {
}
