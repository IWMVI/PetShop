package br.iwmvi.petshop.pagamento.service;

import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.agendamento.repository.AgendamentoRepository;
import br.iwmvi.petshop.common.validator.EntityValidator;
import br.iwmvi.petshop.exception.AgendamentoNotFoundException;
import br.iwmvi.petshop.exception.PagamentoNotFoundException;
import br.iwmvi.petshop.financeiro.service.LancamentoFinanceiroService;
import br.iwmvi.petshop.pagamento.dto.request.AtualizarStatusPagamentoRequest;
import br.iwmvi.petshop.pagamento.dto.request.PagamentoRequest;
import br.iwmvi.petshop.pagamento.dto.response.PagamentoResponse;
import br.iwmvi.petshop.pagamento.mapper.PagamentoMapper;
import br.iwmvi.petshop.pagamento.model.Pagamento;
import br.iwmvi.petshop.pagamento.model.StatusPagamento;
import br.iwmvi.petshop.pagamento.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço de pagamentos.
 *
 * <p>Assim como {@link br.iwmvi.petshop.agendamento.service.AgendamentoService}, não
 * usa {@link br.iwmvi.petshop.common.service.CrudService}: o registro/listagem são
 * aninhados sob agendamento ({@code /agendamentos/{agendamentoId}/pagamentos}), enquanto
 * consulta/atualização/cancelamento são operações flat por id do próprio pagamento
 * ({@code /pagamentos/{id}}), e o "delete" tem semântica própria (status CANCELADO +
 * soft delete).
 *
 * <p>Quando um pagamento transiciona para/de PAGO, um lançamento de ENTRADA é
 * criado/cancelado em {@link LancamentoFinanceiroService}, mantendo o extrato
 * financeiro sincronizado com os pagamentos (ver {@link #atualizarStatus} e
 * {@link #cancelar}).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final EntityValidator<Pagamento> pagamentoValidator;
    private final LancamentoFinanceiroService lancamentoFinanceiroService;

    public PagamentoResponse registrar(Long agendamentoId, PagamentoRequest request) {
        Agendamento agendamento = buscarAgendamentoAtivo(agendamentoId);

        Pagamento pagamento = PagamentoMapper.toEntity(request, agendamento);
        pagamentoValidator.validate(pagamento);

        return PagamentoMapper.toResponse(pagamentoRepository.save(pagamento));
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponse> listarPorAgendamento(Long agendamentoId) {
        buscarAgendamentoAtivo(agendamentoId);

        return pagamentoRepository.findByAgendamentoIdAndDeletedAtIsNull(agendamentoId).stream()
                .map(PagamentoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagamentoResponse buscarPorId(Long id) {
        return PagamentoMapper.toResponse(buscarPagamentoAtivo(id));
    }

    public PagamentoResponse atualizarStatus(Long id, AtualizarStatusPagamentoRequest request) {
        Pagamento pagamento = buscarPagamentoAtivo(id);
        StatusPagamento statusAnterior = pagamento.getStatus();

        pagamento.atualizarStatus(request.status(), request.dataPagamento());
        pagamentoValidator.validate(pagamento);
        Pagamento salvo = pagamentoRepository.save(pagamento);

        sincronizarLancamentoFinanceiro(salvo, statusAnterior);

        return PagamentoMapper.toResponse(salvo);
    }

    public void cancelar(Long id) {
        Pagamento pagamento = buscarPagamentoAtivo(id);
        boolean eraPago = pagamento.getStatus() == StatusPagamento.PAGO;

        pagamento.cancelar();
        pagamentoRepository.save(pagamento);

        if (eraPago) {
            lancamentoFinanceiroService.cancelarPorPagamento(id);
        }
    }

    /**
     * Mantém o extrato financeiro sincronizado com a transição de status do pagamento:
     * cria a entrada ao chegar em PAGO, cancela a entrada ao sair de PAGO. Transições que
     * não cruzam a fronteira PAGO (ex.: PENDENTE→PENDENTE, PAGO→PAGO) não fazem nada, para
     * não duplicar nem recriar o lançamento à toa.
     */
    private void sincronizarLancamentoFinanceiro(Pagamento pagamento, StatusPagamento statusAnterior) {
        boolean ficouPago = statusAnterior != StatusPagamento.PAGO && pagamento.getStatus() == StatusPagamento.PAGO;
        boolean deixouDeSerPago = statusAnterior == StatusPagamento.PAGO && pagamento.getStatus() != StatusPagamento.PAGO;

        if (ficouPago) {
            lancamentoFinanceiroService.registrarEntradaDePagamento(pagamento);
        } else if (deixouDeSerPago) {
            lancamentoFinanceiroService.cancelarPorPagamento(pagamento.getId());
        }
    }

    private Agendamento buscarAgendamentoAtivo(Long agendamentoId) {
        return agendamentoRepository.findActiveById(agendamentoId)
                .orElseThrow(() -> new AgendamentoNotFoundException(agendamentoId));
    }

    private Pagamento buscarPagamentoAtivo(Long id) {
        return pagamentoRepository.findActiveById(id)
                .orElseThrow(() -> new PagamentoNotFoundException(id));
    }
}
