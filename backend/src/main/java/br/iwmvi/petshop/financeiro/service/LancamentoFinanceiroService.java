package br.iwmvi.petshop.financeiro.service;

import br.iwmvi.petshop.common.dto.PaginaResponse;
import br.iwmvi.petshop.common.validator.EntityValidator;
import br.iwmvi.petshop.exception.LancamentoFinanceiroNotFoundException;
import br.iwmvi.petshop.financeiro.dto.request.ContaFinanceiraRequest;
import br.iwmvi.petshop.financeiro.dto.request.LancamentoFinanceiroRequest;
import br.iwmvi.petshop.financeiro.dto.request.MarcarComoPagaRequest;
import br.iwmvi.petshop.financeiro.dto.response.ExtratoResponse;
import br.iwmvi.petshop.financeiro.dto.response.LancamentoFinanceiroResponse;
import br.iwmvi.petshop.financeiro.dto.response.SaldoContasResponse;
import br.iwmvi.petshop.financeiro.mapper.LancamentoFinanceiroMapper;
import br.iwmvi.petshop.financeiro.model.CategoriaLancamento;
import br.iwmvi.petshop.financeiro.model.LancamentoFinanceiro;
import br.iwmvi.petshop.financeiro.model.StatusLancamento;
import br.iwmvi.petshop.financeiro.model.TipoLancamento;
import br.iwmvi.petshop.financeiro.repository.LancamentoFinanceiroRepository;
import br.iwmvi.petshop.exception.LancamentoFinanceiroValidationException;
import br.iwmvi.petshop.pagamento.model.Pagamento;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Serviço do módulo financeiro.
 *
 * <p>Lançamentos manuais (entrada/saída avulsas) têm CRUD completo através
 * deste serviço. Lançamentos gerados automaticamente a partir de um
 * {@link Pagamento} marcado como PAGO ({@link #registrarEntradaDePagamento})
 * não podem ser editados/cancelados diretamente aqui — isso evita que o
 * extrato divirja do estado real do pagamento; a correção deve ser feita
 * mudando o status do pagamento, que por sua vez chama
 * {@link #cancelarPorPagamento} para manter os dois sincronizados.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LancamentoFinanceiroService {

    private final LancamentoFinanceiroRepository repository;
    private final EntityValidator<LancamentoFinanceiro> validator;

    public LancamentoFinanceiroResponse registrar(LancamentoFinanceiroRequest request) {
        LancamentoFinanceiro lancamento = LancamentoFinanceiroMapper.toEntity(request);
        validator.validate(lancamento);
        return LancamentoFinanceiroMapper.toResponse(repository.save(lancamento));
    }

    @Transactional(readOnly = true)
    public LancamentoFinanceiroResponse buscarPorId(Long id) {
        return LancamentoFinanceiroMapper.toResponse(buscarAtivo(id));
    }

    public LancamentoFinanceiroResponse atualizar(Long id, LancamentoFinanceiroRequest request) {
        LancamentoFinanceiro lancamento = buscarAtivo(id);
        garantirQueEhManual(lancamento);

        lancamento.atualizarLancamento(request.tipo(), request.categoria(), request.descricao(), request.valor(), request.dataPagamento());
        validator.validate(lancamento);

        return LancamentoFinanceiroMapper.toResponse(repository.save(lancamento));
    }

    public void cancelar(Long id) {
        LancamentoFinanceiro lancamento = buscarAtivo(id);
        garantirQueEhManual(lancamento);

        lancamento.cancelar();
        repository.save(lancamento);
    }

    @Transactional(readOnly = true)
    public ExtratoResponse extrato(LocalDateTime inicio, LocalDateTime fim, TipoLancamento tipo,
                                   CategoriaLancamento categoria, int pagina, int tamanho) {
        LocalDateTime inicioEfetivo = inicio != null ? inicio : inicioDoMesAtual();
        LocalDateTime fimEfetivo = fim != null ? fim : LocalDateTime.now();

        var pageable = PaginaResponse.pageable(pagina, tamanho,
                Sort.by(Sort.Direction.DESC, "dataPagamento").and(Sort.by(Sort.Direction.DESC, "id")));

        Page<LancamentoFinanceiro> page = repository.buscarExtrato(inicioEfetivo, fimEfetivo, tipo, categoria, pageable);
        BigDecimal totalEntradas = repository.somarPorTipo(TipoLancamento.ENTRADA, inicioEfetivo, fimEfetivo, categoria);
        BigDecimal totalSaidas = repository.somarPorTipo(TipoLancamento.SAIDA, inicioEfetivo, fimEfetivo, categoria);

        return new ExtratoResponse(
                PaginaResponse.of(page.map(LancamentoFinanceiroMapper::toResponse)),
                totalEntradas,
                totalSaidas,
                totalEntradas.subtract(totalSaidas)
        );
    }

    public LancamentoFinanceiroResponse registrarConta(ContaFinanceiraRequest request) {
        LancamentoFinanceiro conta = LancamentoFinanceiroMapper.toEntity(request);
        validator.validate(conta);
        return LancamentoFinanceiroMapper.toResponse(repository.save(conta));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<LancamentoFinanceiroResponse> listarContas(TipoLancamento tipo, StatusLancamento status, int pagina, int tamanho) {
        StatusLancamento statusEfetivo = status != null ? status : StatusLancamento.PENDENTE;
        var pageable = PaginaResponse.pageable(pagina, tamanho, Sort.by("dataVencimento").and(Sort.by("id")));
        Page<LancamentoFinanceiro> page = repository.buscarContas(tipo, statusEfetivo, pageable);
        return PaginaResponse.of(page.map(LancamentoFinanceiroMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public SaldoContasResponse saldoContas(TipoLancamento tipo) {
        return new SaldoContasResponse(
                repository.somarPendentePorTipo(tipo),
                repository.somarVencidoPorTipo(tipo, LocalDateTime.now())
        );
    }

    public LancamentoFinanceiroResponse atualizarConta(Long id, ContaFinanceiraRequest request) {
        LancamentoFinanceiro conta = buscarAtivo(id);
        garantirQueEhManual(conta);
        garantirQuePendente(conta);

        conta.atualizarConta(request.tipo(), request.categoria(), request.descricao(), request.valor(), request.dataVencimento());
        validator.validate(conta);

        return LancamentoFinanceiroMapper.toResponse(repository.save(conta));
    }

    public LancamentoFinanceiroResponse marcarComoPaga(Long id, MarcarComoPagaRequest request) {
        LancamentoFinanceiro conta = buscarAtivo(id);
        garantirQueEhManual(conta);
        garantirQuePendente(conta);

        conta.marcarComoPaga(request.dataPagamento());
        validator.validate(conta);

        return LancamentoFinanceiroMapper.toResponse(repository.save(conta));
    }

    /** Chamado pelo PagamentoService quando um pagamento é marcado como PAGO. */
    public void registrarEntradaDePagamento(Pagamento pagamento) {
        repository.save(LancamentoFinanceiro.deEntradaPagamento(pagamento));
    }

    /** Chamado pelo PagamentoService quando um pagamento vinculado deixa de estar PAGO. */
    public void cancelarPorPagamento(Long pagamentoId) {
        repository.findByPagamentoIdAndDeletedAtIsNull(pagamentoId).ifPresent(lancamento -> {
            lancamento.delete();
            repository.save(lancamento);
        });
    }

    private LancamentoFinanceiro buscarAtivo(Long id) {
        return repository.findActiveById(id)
                .orElseThrow(() -> new LancamentoFinanceiroNotFoundException(id));
    }

    private void garantirQueEhManual(LancamentoFinanceiro lancamento) {
        if (lancamento.getPagamentoId() != null) {
            throw new LancamentoFinanceiroValidationException(
                    "Lançamentos gerados automaticamente a partir de um pagamento não podem ser " +
                            "editados ou cancelados diretamente; altere o status do pagamento correspondente.");
        }
    }

    private void garantirQuePendente(LancamentoFinanceiro lancamento) {
        if (lancamento.getStatus() != StatusLancamento.PENDENTE) {
            throw new LancamentoFinanceiroValidationException("Só é possível editar ou marcar como paga uma conta pendente.");
        }
    }

    private LocalDateTime inicioDoMesAtual() {
        return YearMonth.now().atDay(1).atStartOfDay();
    }
}
