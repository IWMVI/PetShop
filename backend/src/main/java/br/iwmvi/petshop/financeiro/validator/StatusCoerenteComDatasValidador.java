package br.iwmvi.petshop.financeiro.validator;

import br.iwmvi.petshop.common.validator.EntityValidator;
import br.iwmvi.petshop.exception.LancamentoFinanceiroValidationException;
import br.iwmvi.petshop.exception.ValidationException;
import br.iwmvi.petshop.financeiro.model.LancamentoFinanceiro;
import br.iwmvi.petshop.financeiro.model.StatusLancamento;

/**
 * Strategy de validação: as datas do lançamento devem ser coerentes com seu status.
 * Uma conta PENDENTE ainda não tem data de pagamento, mas precisa de vencimento;
 * um lançamento PAGO precisa ter data de pagamento preenchida.
 */
public class StatusCoerenteComDatasValidador implements EntityValidator<LancamentoFinanceiro> {

    @Override
    public void validate(LancamentoFinanceiro lancamento) throws ValidationException {
        if (lancamento.getStatus() == StatusLancamento.PENDENTE) {
            if (lancamento.getDataVencimento() == null) {
                throw new LancamentoFinanceiroValidationException(
                        "Data de vencimento é obrigatória para uma conta pendente.");
            }
            if (lancamento.getDataPagamento() != null) {
                throw new LancamentoFinanceiroValidationException(
                        "Lançamento pendente não pode ter data de pagamento.");
            }
        } else if (lancamento.getStatus() == StatusLancamento.PAGO) {
            if (lancamento.getDataPagamento() == null) {
                throw new LancamentoFinanceiroValidationException(
                        "Data de pagamento é obrigatória para um lançamento realizado.");
            }
        }
    }
}
