package br.iwmvi.petshop.funcionario.dto.request;

import br.iwmvi.petshop.funcionario.model.Cargo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record FuncionarioRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, min = 3)
        String nome,

        @NotBlank(message = "O CPF é obrigatório")
        @CPF(message = "CPF inválido")
        String cpf,

        @NotNull(message = "Cargo é obrigatório.")
        Cargo cargo,

        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(regexp = "\\d{10,11}", message = "Telefone deve conter 10 ou 11 dígitos.")
        String telefone
) {

}
