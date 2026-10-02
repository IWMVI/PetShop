package br.iwmvi.petshop.pagamento.validator;

import br.iwmvi.petshop.common.validator.EntityValidator;
import br.iwmvi.petshop.exception.PagamentoValidationException;
import br.iwmvi.petshop.exception.ValidationException;
import br.iwmvi.petshop.pagamento.model.Pagamento;

import java.math.BigDecimal;

/**
 * Strategy de validação: o valor do pagamento é obrigatório e deve ser maior que zero.
 */
public class ValorPositivoValidador implements EntityValidator<Pagamento> {

    @Override
    public void validate(Pagamento pagamento) throws ValidationException {
        BigDecimal valor = pagamento.getValor();
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PagamentoValidationException("O valor do pagamento deve ser maior que zero.");
        }
    }
}
