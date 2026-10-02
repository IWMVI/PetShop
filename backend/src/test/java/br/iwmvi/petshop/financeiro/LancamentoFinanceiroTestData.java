package br.iwmvi.petshop.financeiro;

import br.iwmvi.petshop.financeiro.dto.request.ContaFinanceiraRequest;
import br.iwmvi.petshop.financeiro.dto.request.LancamentoFinanceiroRequest;
import br.iwmvi.petshop.financeiro.model.CategoriaLancamento;
import br.iwmvi.petshop.financeiro.model.LancamentoFinanceiro;
import br.iwmvi.petshop.financeiro.model.TipoLancamento;
import br.iwmvi.petshop.pagamento.PagamentoTestData;
import br.iwmvi.petshop.pagamento.model.Pagamento;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LancamentoFinanceiroTestData {

    public static LancamentoFinanceiroRequest criarEntradaRequest() {
        return new LancamentoFinanceiroRequest(
                TipoLancamento.ENTRADA,
                CategoriaLancamento.VENDA_PRODUTO,
                "Venda de ração",
                new BigDecimal("80.00"),
                LocalDateTime.now()
        );
    }

    public static LancamentoFinanceiroRequest criarSaidaRequest() {
        return new LancamentoFinanceiroRequest(
                TipoLancamento.SAIDA,
                CategoriaLancamento.FORNECEDOR,
                "Compra de insumos",
                new BigDecimal("200.00"),
                LocalDateTime.now()
        );
    }

    public static LancamentoFinanceiro criarLancamento(Long id, TipoLancamento tipo, CategoriaLancamento categoria, String valor) {
        var lancamento = new LancamentoFinanceiro(tipo, categoria, "Lançamento de teste", new BigDecimal(valor), LocalDateTime.now());
        ReflectionTestUtils.setField(lancamento, "id", id);
        return lancamento;
    }

    public static LancamentoFinanceiro criarLancamentoDePagamento(Long id, Long agendamentoId) {
        var agendamento = PagamentoTestData.criarAgendamentoAtivo(agendamentoId);
        Pagamento pagamento = PagamentoTestData.criarPagamento(900L, agendamento);
        pagamento.atualizarStatus(br.iwmvi.petshop.pagamento.model.StatusPagamento.PAGO, LocalDateTime.now());

        var lancamento = LancamentoFinanceiro.deEntradaPagamento(pagamento);
        ReflectionTestUtils.setField(lancamento, "id", id);
        return lancamento;
    }

    public static ContaFinanceiraRequest criarContaRequest(TipoLancamento tipo, LocalDateTime dataVencimento) {
        if (tipo == TipoLancamento.ENTRADA) {
            return new ContaFinanceiraRequest(
                    TipoLancamento.ENTRADA,
                    CategoriaLancamento.OUTRA_RECEITA,
                    "Conta a receber de teste",
                    new BigDecimal("150.00"),
                    dataVencimento
            );
        }
        return new ContaFinanceiraRequest(
                TipoLancamento.SAIDA,
                CategoriaLancamento.FORNECEDOR,
                "Conta a pagar de teste",
                new BigDecimal("150.00"),
                dataVencimento
        );
    }

    public static LancamentoFinanceiro criarContaPendente(Long id, TipoLancamento tipo, CategoriaLancamento categoria,
                                                           String valor, LocalDateTime dataVencimento) {
        var conta = LancamentoFinanceiro.novaConta(tipo, categoria, "Conta pendente de teste", new BigDecimal(valor), dataVencimento);
        ReflectionTestUtils.setField(conta, "id", id);
        return conta;
    }
}
