package br.iwmvi.petshop.pagamento.validator;

import br.iwmvi.petshop.common.validator.EntityValidator;
import br.iwmvi.petshop.exception.PagamentoValidationException;
import br.iwmvi.petshop.exception.ValidationException;
import br.iwmvi.petshop.pagamento.model.Pagamento;
import br.iwmvi.petshop.pagamento.model.StatusPagamento;

/**
 * Strategy de validação: a data de pagamento só pode estar preenchida quando
 * o status é PAGO, e é obrigatória nesse caso.
 */
public class DataPagamentoCoerenteValidador implements EntityValidator<Pagamento> {

    @Override
    public void validate(Pagamento pagamento) throws ValidationException {
        boolean pago = pagamento.getStatus() == StatusPagamento.PAGO;
        boolean temDataPagamento = pagamento.getDataPagamento() != null;

        if (pago && !temDataPagamento) {
            throw new PagamentoValidationException("Data de pagamento é obrigatória quando o status é PAGO.");
        }
        if (!pago && temDataPagamento) {
            throw new PagamentoValidationException("Data de pagamento só pode ser preenchida quando o status é PAGO.");
        }
    }
}
