package br.iwmvi.petshop.financeiro.dto.request;

import br.iwmvi.petshop.financeiro.model.CategoriaLancamento;
import br.iwmvi.petshop.financeiro.model.TipoLancamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LancamentoFinanceiroRequest(
        @NotNull TipoLancamento tipo,

        @NotNull CategoriaLancamento categoria,

        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = 255, message = "Descrição deve ter até 255 caracteres.")
        String descricao,

        @NotNull @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
        BigDecimal valor,

        @NotNull(message = "A data é obrigatória.")
        LocalDateTime data
) {
}
