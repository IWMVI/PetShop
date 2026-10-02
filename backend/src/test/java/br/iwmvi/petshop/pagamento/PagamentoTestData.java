package br.iwmvi.petshop.pagamento;

import br.iwmvi.petshop.agendamento.AgendamentoTestData;
import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.pagamento.dto.request.AtualizarStatusPagamentoRequest;
import br.iwmvi.petshop.pagamento.dto.request.PagamentoRequest;
import br.iwmvi.petshop.pagamento.model.MetodoPagamento;
import br.iwmvi.petshop.pagamento.model.Pagamento;
import br.iwmvi.petshop.pagamento.model.StatusPagamento;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagamentoTestData {

    public static PagamentoRequest criarPagamentoRequest() {
        return new PagamentoRequest(new BigDecimal("150.00"), MetodoPagamento.PIX);
    }

    public static PagamentoRequest criarPagamentoRequest(BigDecimal valor) {
        return new PagamentoRequest(valor, MetodoPagamento.PIX);
    }

    public static AtualizarStatusPagamentoRequest criarAtualizarStatusRequest(StatusPagamento status, LocalDateTime dataPagamento) {
        return new AtualizarStatusPagamentoRequest(status, dataPagamento);
    }

    public static Agendamento criarAgendamentoAtivo(Long id) {
        var pet = AgendamentoTestData.criarPet();
        var servico = AgendamentoTestData.criarServico(10L, "Banho", "50.00");
        var agendamento = AgendamentoTestData.criarAgendamento(pet, LocalDateTime.now().plusDays(1), java.util.List.of(servico));
        ReflectionTestUtils.setField(agendamento, "id", id);
        return agendamento;
    }

    public static Pagamento criarPagamento(Long id, Agendamento agendamento) {
        var pagamento = new Pagamento(agendamento, new BigDecimal("150.00"), MetodoPagamento.PIX);
        ReflectionTestUtils.setField(pagamento, "id", id);
        return pagamento;
    }
}
