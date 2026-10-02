package br.iwmvi.petshop.endereco.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(
        @NotBlank(message = "CEP é obrigatório.")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP deve estar no formato 01001-001 ou 01001001")
        String cep,

        @NotBlank(message = "Logradouro é obrigatório.")
        String logradouro,

        @Size(max = 20, message = "Número deve ter até 20 caracteres.")
        String numero,

        @Size(max = 100, message = "Complemento deve ter até 100 caracteres.")
        String complemento,

        @NotBlank(message = "Bairro é obrigatório.")
        String bairro,

        @NotBlank(message = "Cidade é obrigatória.")
        String cidade,

        @Size(min = 2, max = 2)
        @Pattern(regexp = "[A-Za-z]{2}", message = "Estado deve ser uma UF válida com 2 letras.")
        String estado
) {
}
