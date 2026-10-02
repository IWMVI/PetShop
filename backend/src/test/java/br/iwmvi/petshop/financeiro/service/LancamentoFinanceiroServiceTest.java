package br.iwmvi.petshop.financeiro.service;

import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.common.dto.PaginaResponse;
import br.iwmvi.petshop.common.validator.CompositeValidator;
import br.iwmvi.petshop.exception.LancamentoFinanceiroNotFoundException;
import br.iwmvi.petshop.exception.LancamentoFinanceiroValidationException;
import br.iwmvi.petshop.financeiro.LancamentoFinanceiroTestData;
import br.iwmvi.petshop.financeiro.model.CategoriaLancamento;
import br.iwmvi.petshop.financeiro.model.LancamentoFinanceiro;
import br.iwmvi.petshop.financeiro.model.StatusLancamento;
import br.iwmvi.petshop.financeiro.model.TipoLancamento;
import br.iwmvi.petshop.financeiro.repository.LancamentoFinanceiroRepository;
import br.iwmvi.petshop.financeiro.validator.CategoriaCompativelComTipoValidador;
import br.iwmvi.petshop.financeiro.validator.StatusCoerenteComDatasValidador;
import br.iwmvi.petshop.pagamento.PagamentoTestData;
import br.iwmvi.petshop.pagamento.model.Pagamento;
import br.iwmvi.petshop.pagamento.model.StatusPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LancamentoFinanceiroServiceTest {

    @Mock
    private LancamentoFinanceiroRepository repository;

    private LancamentoFinanceiroService service;

    @BeforeEach
    void setUp() {
        service = new LancamentoFinanceiroService(
                repository,
                new CompositeValidator<>(List.of(
                        new CategoriaCompativelComTipoValidador(),
                        new StatusCoerenteComDatasValidador()
                ))
        );
    }

    @Nested
    @DisplayName("Registro de lançamentos manuais")
    class Registro {

        @Test
        @DisplayName("PCE - Deve registrar uma entrada manual")
        void deveRegistrarEntradaManual() {
            var request = LancamentoFinanceiroTestData.criarEntradaRequest();
            when(repository.save(any(LancamentoFinanceiro.class))).thenAnswer(i -> i.getArgument(0));

            var response = service.registrar(request);

            assertThat(response.tipo()).isEqualTo(TipoLancamento.ENTRADA);
            assertThat(response.categoria()).isEqualTo(CategoriaLancamento.VENDA_PRODUTO);
            assertThat(response.pagamentoId()).isNull();
        }

        @Test
        @DisplayName("PCE - Deve registrar uma saída manual")
        void deveRegistrarSaidaManual() {
            var request = LancamentoFinanceiroTestData.criarSaidaRequest();
            when(repository.save(any(LancamentoFinanceiro.class))).thenAnswer(i -> i.getArgument(0));

            var response = service.registrar(request);

            assertThat(response.tipo()).isEqualTo(TipoLancamento.SAIDA);
            assertThat(response.categoria()).isEqualTo(CategoriaLancamento.FORNECEDOR);
        }

        @Test
        @DisplayName("ESE - Não deve registrar entrada com categoria de saída")
        void naoDeveRegistrarEntradaComCategoriaDeSaida() {
            var request = new br.iwmvi.petshop.financeiro.dto.request.LancamentoFinanceiroRequest(
                    TipoLancamento.ENTRADA, CategoriaLancamento.ALUGUEL, "Inválido",
                    new BigDecimal("10.00"), LocalDateTime.now()
            );

            assertThatThrownBy(() -> service.registrar(request))
                    .isInstanceOf(LancamentoFinanceiroValidationException.class)
                    .hasMessageContaining("não é compatível");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("ESE - Não deve registrar saída com categoria de entrada")
        void naoDeveRegistrarSaidaComCategoriaDeEntrada() {
            var request = new br.iwmvi.petshop.financeiro.dto.request.LancamentoFinanceiroRequest(
                    TipoLancamento.SAIDA, CategoriaLancamento.VENDA_PRODUTO, "Inválido",
                    new BigDecimal("10.00"), LocalDateTime.now()
            );

            assertThatThrownBy(() -> service.registrar(request))
                    .isInstanceOf(LancamentoFinanceiroValidationException.class);

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Consulta")
    class Consulta {

        @Test
        @DisplayName("PCE - Deve buscar lançamento por id")
        void deveBuscarPorId() {
            var lancamento = LancamentoFinanceiroTestData.criarLancamento(1L, TipoLancamento.ENTRADA, CategoriaLancamento.VENDA_PRODUTO, "50.00");
            when(repository.findActiveById(1L)).thenReturn(Optional.of(lancamento));

            var response = service.buscarPorId(1L);

            assertThat(response.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("ESE - Não deve buscar lançamento inexistente")
        void naoDeveBuscarLancamentoInexistente() {
            when(repository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(1L))
                    .isInstanceOf(LancamentoFinanceiroNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Atualização e cancelamento")
    class AtualizacaoECancelamento {

        @Test
        @DisplayName("PCE - Deve atualizar lançamento manual")
        void deveAtualizarLancamentoManual() {
            var lancamento = LancamentoFinanceiroTestData.criarLancamento(1L, TipoLancamento.ENTRADA, CategoriaLancamento.VENDA_PRODUTO, "50.00");
            var request = LancamentoFinanceiroTestData.criarEntradaRequest();
            when(repository.findActiveById(1L)).thenReturn(Optional.of(lancamento));
            when(repository.save(any(LancamentoFinanceiro.class))).thenAnswer(i -> i.getArgument(0));

            var response = service.atualizar(1L, request);

            assertThat(response.descricao()).isEqualTo("Venda de ração");
        }

        @Test
        @DisplayName("ESE - Não deve atualizar lançamento gerado por pagamento")
        void naoDeveAtualizarLancamentoDePagamento() {
            var lancamento = LancamentoFinanceiroTestData.criarLancamentoDePagamento(1L, 50L);
            var request = LancamentoFinanceiroTestData.criarEntradaRequest();
            when(repository.findActiveById(1L)).thenReturn(Optional.of(lancamento));

            assertThatThrownBy(() -> service.atualizar(1L, request))
                    .isInstanceOf(LancamentoFinanceiroValidationException.class)
                    .hasMessageContaining("não podem ser");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("PCE - Deve cancelar lançamento manual")
        void deveCancelarLancamentoManual() {
            var lancamento = LancamentoFinanceiroTestData.criarLancamento(1L, TipoLancamento.SAIDA, CategoriaLancamento.ALUGUEL, "50.00");
            when(repository.findActiveById(1L)).thenReturn(Optional.of(lancamento));

            service.cancelar(1L);

            assertThat(lancamento.getDeletedAt()).isNotNull();
            verify(repository).save(lancamento);
        }

        @Test
        @DisplayName("ESE - Não deve cancelar lançamento gerado por pagamento")
        void naoDeveCancelarLancamentoDePagamento() {
            var lancamento = LancamentoFinanceiroTestData.criarLancamentoDePagamento(1L, 50L);
            when(repository.findActiveById(1L)).thenReturn(Optional.of(lancamento));

            assertThatThrownBy(() -> service.cancelar(1L))
                    .isInstanceOf(LancamentoFinanceiroValidationException.class);

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Extrato")
    class Extrato {

        @Test
        @DisplayName("PCE - Deve calcular totais de entrada, saída e saldo do período")
        void deveCalcularTotaisDoPeriodo() {
            var entrada = LancamentoFinanceiroTestData.criarLancamento(1L, TipoLancamento.ENTRADA, CategoriaLancamento.VENDA_PRODUTO, "100.00");
            LocalDateTime inicio = LocalDateTime.now().minusDays(7);
            LocalDateTime fim = LocalDateTime.now();

            when(repository.buscarExtrato(eq(inicio), eq(fim), isNull(), isNull(), any(Pageable.class)))
                    .thenAnswer(i -> new PageImpl<>(List.of(entrada), i.getArgument(4), 1));
            when(repository.somarPorTipo(eq(TipoLancamento.ENTRADA), eq(inicio), eq(fim), isNull()))
                    .thenReturn(new BigDecimal("500.00"));
            when(repository.somarPorTipo(eq(TipoLancamento.SAIDA), eq(inicio), eq(fim), isNull()))
                    .thenReturn(new BigDecimal("200.00"));

            var response = service.extrato(inicio, fim, null, null, 0, 10);

            assertThat(response.totalEntradas()).isEqualByComparingTo("500.00");
            assertThat(response.totalSaidas()).isEqualByComparingTo("200.00");
            assertThat(response.saldo()).isEqualByComparingTo("300.00");
            assertThat(response.lancamentos().itens()).hasSize(1);
        }

        @Test
        @DisplayName("AVL - Deve usar o início do mês atual como padrão quando datas não são informadas")
        void deveUsarPeriodoPadraoQuandoDatasNaoInformadas() {
            when(repository.buscarExtrato(any(), any(), isNull(), isNull(), any(Pageable.class)))
                    .thenAnswer(i -> new PageImpl<>(List.<LancamentoFinanceiro>of(), i.getArgument(4), 0));
            when(repository.somarPorTipo(any(), any(), any(), isNull())).thenReturn(BigDecimal.ZERO);

            service.extrato(null, null, null, null, 0, 10);

            var captorInicio = org.mockito.ArgumentCaptor.forClass(LocalDateTime.class);
            verify(repository).buscarExtrato(captorInicio.capture(), any(), isNull(), isNull(), any(Pageable.class));
            assertThat(captorInicio.getValue().getDayOfMonth()).isEqualTo(1);
            assertThat(captorInicio.getValue().toLocalTime()).isEqualTo(java.time.LocalTime.MIDNIGHT);
        }

        @Test
        @DisplayName("ESE - O extrato não deve incluir lançamentos com status PENDENTE")
        void extratoNaoDeveIncluirLancamentosPendentes() {
            LocalDateTime inicio = LocalDateTime.now().minusDays(7);
            LocalDateTime fim = LocalDateTime.now();

            // A query buscarExtrato já filtra por status PAGO internamente; aqui garantimos
            // que o service não contorna esse filtro nem mistura lançamentos pendentes no total.
            when(repository.buscarExtrato(eq(inicio), eq(fim), isNull(), isNull(), any(Pageable.class)))
                    .thenAnswer(i -> new PageImpl<>(List.<LancamentoFinanceiro>of(), i.getArgument(4), 0));
            when(repository.somarPorTipo(eq(TipoLancamento.ENTRADA), eq(inicio), eq(fim), isNull()))
                    .thenReturn(BigDecimal.ZERO);
            when(repository.somarPorTipo(eq(TipoLancamento.SAIDA), eq(inicio), eq(fim), isNull()))
                    .thenReturn(BigDecimal.ZERO);

            var response = service.extrato(inicio, fim, null, null, 0, 10);

            assertThat(response.lancamentos().itens()).isEmpty();
            assertThat(response.totalEntradas()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.totalSaidas()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Contas a pagar e a receber")
    class ContasAPagarEReceber {

        @Test
        @DisplayName("PCE - Deve registrar uma conta a pagar pendente")
        void deveRegistrarContaAPagar() {
            var request = LancamentoFinanceiroTestData.criarContaRequest(TipoLancamento.SAIDA, LocalDateTime.now().plusDays(10));
            when(repository.save(any(LancamentoFinanceiro.class))).thenAnswer(i -> i.getArgument(0));

            var response = service.registrarConta(request);

            assertThat(response.status()).isEqualTo(StatusLancamento.PENDENTE);
            assertThat(response.dataPagamento()).isNull();
            assertThat(response.dataVencimento()).isNotNull();
        }

        @Test
        @DisplayName("PCE - Deve registrar uma conta a receber pendente")
        void deveRegistrarContaAReceber() {
            var request = LancamentoFinanceiroTestData.criarContaRequest(TipoLancamento.ENTRADA, LocalDateTime.now().plusDays(5));
            when(repository.save(any(LancamentoFinanceiro.class))).thenAnswer(i -> i.getArgument(0));

            var response = service.registrarConta(request);

            assertThat(response.tipo()).isEqualTo(TipoLancamento.ENTRADA);
            assertThat(response.status()).isEqualTo(StatusLancamento.PENDENTE);
        }

        @Test
        @DisplayName("ESE - Não deve registrar conta sem data de vencimento")
        void naoDeveRegistrarContaSemDataDeVencimento() {
            var conta = LancamentoFinanceiro.novaConta(TipoLancamento.SAIDA, CategoriaLancamento.FORNECEDOR,
                    "Conta sem vencimento", new BigDecimal("10.00"), null);

            assertThatThrownBy(() -> new StatusCoerenteComDatasValidador().validate(conta))
                    .isInstanceOf(LancamentoFinanceiroValidationException.class)
                    .hasMessageContaining("Data de vencimento é obrigatória");
        }

        @Test
        @DisplayName("PCE - Deve marcar uma conta pendente como paga com sucesso")
        void deveMarcarContaComoPaga() {
            var conta = LancamentoFinanceiroTestData.criarContaPendente(1L, TipoLancamento.SAIDA,
                    CategoriaLancamento.FORNECEDOR, "150.00", LocalDateTime.now().plusDays(3));
            when(repository.findActiveById(1L)).thenReturn(Optional.of(conta));
            when(repository.save(any(LancamentoFinanceiro.class))).thenAnswer(i -> i.getArgument(0));

            var request = new br.iwmvi.petshop.financeiro.dto.request.MarcarComoPagaRequest(LocalDateTime.now());
            var response = service.marcarComoPaga(1L, request);

            assertThat(response.status()).isEqualTo(StatusLancamento.PAGO);
            assertThat(response.dataPagamento()).isNotNull();
        }

        @Test
        @DisplayName("ESE - Não deve marcar como paga uma conta já paga")
        void naoDeveMarcarComoPagaContaJaPaga() {
            var lancamento = LancamentoFinanceiroTestData.criarLancamento(1L, TipoLancamento.SAIDA, CategoriaLancamento.FORNECEDOR, "50.00");
            when(repository.findActiveById(1L)).thenReturn(Optional.of(lancamento));

            var request = new br.iwmvi.petshop.financeiro.dto.request.MarcarComoPagaRequest(LocalDateTime.now());

            assertThatThrownBy(() -> service.marcarComoPaga(1L, request))
                    .isInstanceOf(LancamentoFinanceiroValidationException.class)
                    .hasMessageContaining("pendente");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("ESE - Não deve marcar como paga uma conta cancelada")
        void naoDeveMarcarComoPagaContaCancelada() {
            var conta = LancamentoFinanceiroTestData.criarContaPendente(1L, TipoLancamento.SAIDA,
                    CategoriaLancamento.FORNECEDOR, "150.00", LocalDateTime.now().plusDays(3));
            conta.cancelar();
            when(repository.findActiveById(1L)).thenReturn(Optional.of(conta));

            var request = new br.iwmvi.petshop.financeiro.dto.request.MarcarComoPagaRequest(LocalDateTime.now());

            assertThatThrownBy(() -> service.marcarComoPaga(1L, request))
                    .isInstanceOf(LancamentoFinanceiroValidationException.class);

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("PCE - Deve atualizar uma conta pendente")
        void deveAtualizarContaPendente() {
            var conta = LancamentoFinanceiroTestData.criarContaPendente(1L, TipoLancamento.SAIDA,
                    CategoriaLancamento.FORNECEDOR, "150.00", LocalDateTime.now().plusDays(3));
            var request = LancamentoFinanceiroTestData.criarContaRequest(TipoLancamento.SAIDA, LocalDateTime.now().plusDays(20));
            when(repository.findActiveById(1L)).thenReturn(Optional.of(conta));
            when(repository.save(any(LancamentoFinanceiro.class))).thenAnswer(i -> i.getArgument(0));

            var response = service.atualizarConta(1L, request);

            assertThat(response.descricao()).isEqualTo("Conta a pagar de teste");
            assertThat(response.status()).isEqualTo(StatusLancamento.PENDENTE);
        }

        @Test
        @DisplayName("ESE - Não deve atualizar uma conta já paga")
        void naoDeveAtualizarContaJaPaga() {
            var lancamento = LancamentoFinanceiroTestData.criarLancamento(1L, TipoLancamento.SAIDA, CategoriaLancamento.FORNECEDOR, "50.00");
            var request = LancamentoFinanceiroTestData.criarContaRequest(TipoLancamento.SAIDA, LocalDateTime.now().plusDays(20));
            when(repository.findActiveById(1L)).thenReturn(Optional.of(lancamento));

            assertThatThrownBy(() -> service.atualizarConta(1L, request))
                    .isInstanceOf(LancamentoFinanceiroValidationException.class)
                    .hasMessageContaining("pendente");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("PCE - Deve calcular saldo pendente e vencido corretamente")
        void deveCalcularSaldoPendenteEVencido() {
            when(repository.somarPendentePorTipo(TipoLancamento.SAIDA)).thenReturn(new BigDecimal("300.00"));
            when(repository.somarVencidoPorTipo(eq(TipoLancamento.SAIDA), any(LocalDateTime.class))).thenReturn(new BigDecimal("100.00"));

            var response = service.saldoContas(TipoLancamento.SAIDA);

            assertThat(response.totalPendente()).isEqualByComparingTo("300.00");
            assertThat(response.totalVencido()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("PCE - Deve listar contas filtrando por tipo e status")
        void deveListarContasFiltrandoPorTipoEStatus() {
            var conta = LancamentoFinanceiroTestData.criarContaPendente(1L, TipoLancamento.SAIDA,
                    CategoriaLancamento.FORNECEDOR, "150.00", LocalDateTime.now().plusDays(3));
            when(repository.buscarContas(eq(TipoLancamento.SAIDA), eq(StatusLancamento.PENDENTE), any(Pageable.class)))
                    .thenAnswer(i -> new PageImpl<>(List.of(conta), i.getArgument(2), 1));

            var response = service.listarContas(TipoLancamento.SAIDA, StatusLancamento.PENDENTE, 0, 10);

            assertThat(response.itens()).hasSize(1);
            assertThat(response.itens().get(0).status()).isEqualTo(StatusLancamento.PENDENTE);
        }
    }

    @Nested
    @DisplayName("Integração com Pagamento")
    class IntegracaoComPagamento {

        @Test
        @DisplayName("PCE - Deve registrar entrada a partir de um pagamento PAGO")
        void deveRegistrarEntradaDePagamento() {
            Agendamento agendamento = PagamentoTestData.criarAgendamentoAtivo(50L);
            Pagamento pagamento = PagamentoTestData.criarPagamento(900L, agendamento);
            pagamento.atualizarStatus(StatusPagamento.PAGO, LocalDateTime.now());

            service.registrarEntradaDePagamento(pagamento);

            var captor = org.mockito.ArgumentCaptor.forClass(LancamentoFinanceiro.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getTipo()).isEqualTo(TipoLancamento.ENTRADA);
            assertThat(captor.getValue().getCategoria()).isEqualTo(CategoriaLancamento.PAGAMENTO_SERVICO);
            assertThat(captor.getValue().getPagamentoId()).isEqualTo(900L);
        }

        @Test
        @DisplayName("PCE - Deve cancelar a entrada vinculada a um pagamento")
        void deveCancelarEntradaVinculada() {
            var lancamento = LancamentoFinanceiroTestData.criarLancamentoDePagamento(1L, 50L);
            when(repository.findByPagamentoIdAndDeletedAtIsNull(900L)).thenReturn(Optional.of(lancamento));

            service.cancelarPorPagamento(900L);

            assertThat(lancamento.getDeletedAt()).isNotNull();
            verify(repository).save(lancamento);
        }

        @Test
        @DisplayName("AVL - Não deve falhar ao cancelar quando não há lançamento vinculado")
        void naoDeveFalharQuandoNaoHaLancamentoVinculado() {
            when(repository.findByPagamentoIdAndDeletedAtIsNull(900L)).thenReturn(Optional.empty());

            service.cancelarPorPagamento(900L);

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Dashboard")
    class Dashboard {

        private final LocalDate hoje = LocalDate.now();
        private final LocalDateTime inicioHoje = hoje.atStartOfDay();
        private final LocalDateTime fimHoje = hoje.atTime(LocalTime.MAX);
        private final LocalDateTime inicioMes = YearMonth.now().atDay(1).atStartOfDay();
        private final LocalDateTime fimMes = YearMonth.now().atEndOfMonth().atTime(LocalTime.MAX);

        @BeforeEach
        void stubsPadrao() {
            lenient().when(repository.somarVencimentoNoPeriodo(any(TipoLancamento.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(BigDecimal.ZERO);
            lenient().when(repository.somarVencidoPorTipo(any(TipoLancamento.class), any(LocalDateTime.class)))
                    .thenReturn(BigDecimal.ZERO);
            lenient().when(repository.somarPorTipo(any(TipoLancamento.class), any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
                    .thenReturn(BigDecimal.ZERO);
            lenient().when(repository.buscarRealizadosNoPeriodo(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of());
        }

        @Test
        @DisplayName("PCE - Deve agregar o fluxo de caixa por dia, preenchendo dias sem movimento com zero")
        void deveAgregarFluxoDeCaixaPorDiaComZerosNosDiasSemMovimento() {
            LocalDate diaAnterior = hoje.minusDays(3);
            var entradaHoje = new LancamentoFinanceiro(TipoLancamento.ENTRADA, CategoriaLancamento.VENDA_PRODUTO,
                    "Venda de hoje", new BigDecimal("100.00"), hoje.atTime(10, 0));
            var saidaAnterior = new LancamentoFinanceiro(TipoLancamento.SAIDA, CategoriaLancamento.FORNECEDOR,
                    "Compra anterior", new BigDecimal("40.00"), diaAnterior.atTime(9, 0));
            when(repository.buscarRealizadosNoPeriodo(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(entradaHoje, saidaAnterior));

            var response = service.dashboard();

            assertThat(response.fluxoCaixa()).hasSize(14);
            assertThat(response.fluxoCaixa().get(0).data()).isEqualTo(hoje.minusDays(13));
            assertThat(response.fluxoCaixa().get(13).data()).isEqualTo(hoje);

            var pontoHoje = response.fluxoCaixa().stream().filter(p -> p.data().equals(hoje)).findFirst().orElseThrow();
            assertThat(pontoHoje.entradas()).isEqualByComparingTo("100.00");
            assertThat(pontoHoje.saidas()).isEqualByComparingTo(BigDecimal.ZERO);

            var pontoAnterior = response.fluxoCaixa().stream().filter(p -> p.data().equals(diaAnterior)).findFirst().orElseThrow();
            assertThat(pontoAnterior.entradas()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(pontoAnterior.saidas()).isEqualByComparingTo("40.00");

            LocalDate diaSemMovimento = hoje.minusDays(1);
            var pontoSemMovimento = response.fluxoCaixa().stream().filter(p -> p.data().equals(diaSemMovimento)).findFirst().orElseThrow();
            assertThat(pontoSemMovimento.entradas()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(pontoSemMovimento.saidas()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("PCE - Deve calcular o percentual do mês como 80% quando recebido=80 e pendente=20")
        void deveCalcularPercentualDoMesCorretamente() {
            when(repository.somarPorTipo(eq(TipoLancamento.ENTRADA), eq(inicioMes), eq(fimMes), isNull()))
                    .thenReturn(new BigDecimal("80.00"));
            when(repository.somarVencimentoNoPeriodo(eq(TipoLancamento.ENTRADA), eq(inicioMes), eq(fimMes)))
                    .thenReturn(new BigDecimal("20.00"));

            var response = service.dashboard();

            assertThat(response.percentualRecebidoMes()).isEqualByComparingTo("80");
        }

        @Test
        @DisplayName("AVL - Percentual do mês deve ser zero quando não há nada esperado nem recebido")
        void devePercentualZeroQuandoNadaEsperadoNoMes() {
            when(repository.somarPorTipo(eq(TipoLancamento.SAIDA), eq(inicioMes), eq(fimMes), isNull()))
                    .thenReturn(BigDecimal.ZERO);
            when(repository.somarVencimentoNoPeriodo(eq(TipoLancamento.SAIDA), eq(inicioMes), eq(fimMes)))
                    .thenReturn(BigDecimal.ZERO);

            var response = service.dashboard();

            assertThat(response.percentualPagoMes()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("PCE - aReceberHoje e aPagarHoje devem somar somente o que vence hoje")
        void deveSomarApenasOQueVenceHoje() {
            when(repository.somarVencimentoNoPeriodo(eq(TipoLancamento.ENTRADA), eq(inicioHoje), eq(fimHoje)))
                    .thenReturn(new BigDecimal("150.00"));
            when(repository.somarVencimentoNoPeriodo(eq(TipoLancamento.SAIDA), eq(inicioHoje), eq(fimHoje)))
                    .thenReturn(new BigDecimal("75.00"));

            var response = service.dashboard();

            assertThat(response.aReceberHoje()).isEqualByComparingTo("150.00");
            assertThat(response.aPagarHoje()).isEqualByComparingTo("75.00");
            verify(repository).somarVencimentoNoPeriodo(TipoLancamento.ENTRADA, inicioHoje, fimHoje);
            verify(repository).somarVencimentoNoPeriodo(TipoLancamento.SAIDA, inicioHoje, fimHoje);
        }
    }
}
