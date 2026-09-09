package br.iwmvi.petshop.tutor.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TutorRequest(
        @NotBlank(message = "Nome é obrigatório.")
        String nome,

        @Email
        @NotBlank(message = "E-mail é obrigatório.")
        String email,

        @NotBlank(message = "Telefone é obrigatório.")
        @Pattern(regexp = "\\d{10,11}", message = "Telefone deve conter 10 ou 11 dígitos.")
        String telefone,

        @Valid
        @NotNull(message = "Endereço é obrigatório.")
        EnderecoRequest endereco
) {
}
