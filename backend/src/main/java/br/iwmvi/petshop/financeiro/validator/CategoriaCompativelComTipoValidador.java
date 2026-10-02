package br.iwmvi.petshop.financeiro.validator;

import br.iwmvi.petshop.common.validator.EntityValidator;
import br.iwmvi.petshop.exception.LancamentoFinanceiroValidationException;
import br.iwmvi.petshop.exception.ValidationException;
import br.iwmvi.petshop.financeiro.model.CategoriaLancamento;
import br.iwmvi.petshop.financeiro.model.LancamentoFinanceiro;
import br.iwmvi.petshop.financeiro.model.TipoLancamento;

import java.util.EnumSet;
import java.util.Set;

/**
 * Strategy de validação: a categoria do lançamento deve ser compatível com
 * o tipo (ENTRADA ou SAIDA).
 */
public class CategoriaCompativelComTipoValidador implements EntityValidator<LancamentoFinanceiro> {

    private static final Set<CategoriaLancamento> CATEGORIAS_ENTRADA = EnumSet.of(
            CategoriaLancamento.PAGAMENTO_SERVICO,
            CategoriaLancamento.VENDA_PRODUTO,
            CategoriaLancamento.OUTRA_RECEITA
    );

    private static final Set<CategoriaLancamento> CATEGORIAS_SAIDA = EnumSet.of(
            CategoriaLancamento.ALUGUEL,
            CategoriaLancamento.SALARIO,
            CategoriaLancamento.FORNECEDOR,
            CategoriaLancamento.MANUTENCAO,
            CategoriaLancamento.IMPOSTO,
            CategoriaLancamento.OUTRA_DESPESA
    );

    @Override
    public void validate(LancamentoFinanceiro lancamento) throws ValidationException {
        Set<CategoriaLancamento> categoriasValidas = lancamento.getTipo() == TipoLancamento.ENTRADA
                ? CATEGORIAS_ENTRADA
                : CATEGORIAS_SAIDA;

        if (!categoriasValidas.contains(lancamento.getCategoria())) {
            throw new LancamentoFinanceiroValidationException(
                    "Categoria " + lancamento.getCategoria() + " não é compatível com o tipo " + lancamento.getTipo() + ".");
        }
    }
}
