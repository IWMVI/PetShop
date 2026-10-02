package br.iwmvi.petshop.pagamento.service;

import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.agendamento.repository.AgendamentoRepository;
import br.iwmvi.petshop.common.validator.CompositeValidator;
import br.iwmvi.petshop.exception.AgendamentoNotFoundException;
import br.iwmvi.petshop.exception.PagamentoNotFoundException;
import br.iwmvi.petshop.exception.PagamentoValidationException;
import br.iwmvi.petshop.pagamento.PagamentoTestData;
import br.iwmvi.petshop.pagamento.model.MetodoPagamento;
import br.iwmvi.petshop.pagamento.model.Pagamento;
import br.iwmvi.petshop.pagamento.model.StatusPagamento;
import br.iwmvi.petshop.pagamento.repository.PagamentoRepository;
import br.iwmvi.petshop.pagamento.validator.DataPagamentoCoerenteValidador;
import br.iwmvi.petshop.pagamento.validator.ValorPositivoValidador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    private PagamentoService pagamentoService;

    @BeforeEach
    void setUp() {
        pagamentoService = new PagamentoService(
                pagamentoRepository,
                agendamentoRepository,
                new CompositeValidator<>(List.of(
                        new ValorPositivoValidador(),
                        new DataPagamentoCoerenteValidador()
                ))
        );
    }

    @Nested
    @DisplayName("Registro de pagamentos")
    class Registro {

        @Test
        @DisplayName("PCE - Deve registrar pagamento com sucesso")
        void deveRegistrarPagamentoComSucesso() {
            Agendamento agendamento = PagamentoTestData.criarAgendamentoAtivo(50L);
            var request = PagamentoTestData.criarPagamentoRequest();

            when(agendamentoRepository.findActiveById(50L)).thenReturn(Optional.of(agendamento));
            when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(i -> i.getArgument(0));

            var response = pagamentoService.registrar(50L, request);

            assertThat(response.agendamentoId()).isEqualTo(50L);
            assertThat(response.status()).isEqualTo(StatusPagamento.PENDENTE);
            assertThat(response.metodoPagamento()).isEqualTo(MetodoPagamento.PIX);
            assertThat(response.dataPagamento()).isNull();
            verify(pagamentoRepository).save(any(Pagamento.class));
        }

        @Test
        @DisplayName("ESE - Não deve registrar pagamento com agendamento inexistente")
        void naoDeveRegistrarComAgendamentoInexistente() {
            var request = PagamentoTestData.criarPagamentoRequest();

            when(agendamentoRepository.findActiveById(50L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pagamentoService.registrar(50L, request))
                    .isInstanceOf(AgendamentoNotFoundException.class);

            verify(pagamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("ESE - Não deve registrar pagamento com valor zero ou negativo")
        void naoDeveRegistrarComValorInvalido() {
            Agendamento agendamento = PagamentoTestData.criarAgendamentoAtivo(50L);
            var request = PagamentoTestData.criarPagamentoRequest(BigDecimal.ZERO);

            when(agendamentoRepository.findActiveById(50L)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> pagamentoService.registrar(50L, request))
                    .isInstanceOf(PagamentoValidationException.class)
                    .hasMessageContaining("maior que zero");

            verify(pagamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("AVL - Deve aceitar valor no limite mínimo (0.01)")
        void deveAceitarValorNoLimiteMinimo() {
            Agendamento agendamento = PagamentoTestData.criarAgendamentoAtivo(50L);
            var request = PagamentoTestData.criarPagamentoRequest(new BigDecimal("0.01"));

            when(agendamentoRepository.findActiveById(50L)).thenReturn(Optional.of(agendamento));
            when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(i -> i.getArgument(0));

            var response = pagamentoService.registrar(50L, request);

            assertThat(response.valor()).isEqualByComparingTo("0.01");
        }
    }

    @Nested
    @DisplayName("Consulta de pagamentos")
    class Consulta {

        @Test
        @DisplayName("PCE - Deve listar pagamentos de um agendamento")
        void deveListarPagamentosDoAgendamento() {
            Agendamento agendamento = PagamentoTestData.criarAgendamentoAtivo(50L);
            Pagamento pagamento = PagamentoTestData.criarPagamento(1L, agendamento);

            when(agendamentoRepository.findActiveById(50L)).thenReturn(Optional.of(agendamento));
            when(pagamentoRepository.findByAgendamentoIdAndDeletedAtIsNull(50L)).thenReturn(List.of(pagamento));

            var response = pagamentoService.listarPorAgendamento(50L);

            assertThat(response).hasSize(1);
            assertThat(response.getFirst().agendamentoId()).isEqualTo(50L);
        }

        @Test
        @DisplayName("PCE - Deve retornar lista vazia quando agendamento não tem pagamentos")
        void deveRetornarListaVaziaQuandoNaoHaPagamentos() {
            Agendamento agendamento = PagamentoTestData.criarAgendamentoAtivo(50L);

            when(agendamentoRepository.findActiveById(50L)).thenReturn(Optional.of(agendamento));
            when(pagamentoRepository.findByAgendamentoIdAndDeletedAtIsNull(50L)).thenReturn(List.of());

            var response = pagamentoService.listarPorAgendamento(50L);

            assertThat(response).isEmpty();
        }

        @Test
        @DisplayName("ESE - Não deve listar pagamentos de agendamento inexistente")
        void naoDeveListarQuandoAgendamentoNaoExiste() {
            when(agendamentoRepository.findActiveById(50L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pagamentoService.listarPorAgendamento(50L))
                    .isInstanceOf(AgendamentoNotFoundException.class);

            verifyNoInteractions(pagamentoRepository);
        }

        @Test
        @DisplayName("PCE - Deve buscar pagamento por id")
        void deveBuscarPagamentoPorId() {
            Agendamento agendamento = PagamentoTestData.criarAgendamentoAtivo(50L);
            Pagamento pagamento = PagamentoTestData.criarPagamento(1L, agendamento);

            when(pagamentoRepository.findActiveById(1L)).thenReturn(Optional.of(pagamento));

            var response = pagamentoService.buscarPorId(1L);

            assertThat(response.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("ESE - Não deve buscar pagamento inexistente")
        void naoDeveBuscarPagamentoInexistente() {
            when(pagamentoRepository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pagamentoService.buscarPorId(1L))
                    .isInstanceOf(PagamentoNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Atualização de status")
    class AtualizacaoStatus {

        @Test
        @DisplayName("PCE - Deve atualizar status para PAGO com data de pagamento")
        void deveAtualizarStatusParaPago() {
            Agendamento agendamento = PagamentoTestData.criarAgendamentoAtivo(50L);
            Pagamento pagamento = PagamentoTestData.criarPagamento(1L, agendamento);
            LocalDateTime dataPagamento = LocalDateTime.now();
            var request = PagamentoTestData.criarAtualizarStatusRequest(StatusPagamento.PAGO, dataPagamento);

            when(pagamentoRepository.findActiveById(1L)).thenReturn(Optional.of(pagamento));
            when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(i -> i.getArgument(0));

            var response = pagamentoService.atualizarStatus(1L, request);

            assertThat(response.status()).isEqualTo(StatusPagamento.PAGO);
            assertThat(response.dataPagamento()).isEqualTo(dataPagamento);
        }

        @Test
        @DisplayName("ESE - Não deve marcar como PAGO sem data de pagamento")
        void naoDeveMarcarPagoSemDataPagamento() {
            Agendamento agendamento = PagamentoTestData.criarAgendamentoAtivo(50L);
            Pagamento pagamento = PagamentoTestData.criarPagamento(1L, agendamento);
            var request = PagamentoTestData.criarAtualizarStatusRequest(StatusPagamento.PAGO, null);

            when(pagamentoRepository.findActiveById(1L)).thenReturn(Optional.of(pagamento));

            assertThatThrownBy(() -> pagamentoService.atualizarStatus(1L, request))
                    .isInstanceOf(PagamentoValidationException.class)
                    .hasMessageContaining("obrigatória");

            verify(pagamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("ESE - Não deve informar data de pagamento com status diferente de PAGO")
        void naoDevePermitirDataPagamentoForaDoStatusPago() {
            Agendamento agendamento = PagamentoTestData.criarAgendamentoAtivo(50L);
            Pagamento pagamento = PagamentoTestData.criarPagamento(1L, agendamento);
            var request = PagamentoTestData.criarAtualizarStatusRequest(StatusPagamento.PENDENTE, LocalDateTime.now());

            when(pagamentoRepository.findActiveById(1L)).thenReturn(Optional.of(pagamento));

            assertThatThrownBy(() -> pagamentoService.atualizarStatus(1L, request))
                    .isInstanceOf(PagamentoValidationException.class)
                    .hasMessageContaining("só pode ser preenchida");

            verify(pagamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("ESE - Não deve atualizar pagamento inexistente")
        void naoDeveAtualizarPagamentoInexistente() {
            var request = PagamentoTestData.criarAtualizarStatusRequest(StatusPagamento.PAGO, LocalDateTime.now());

            when(pagamentoRepository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pagamentoService.atualizarStatus(1L, request))
                    .isInstanceOf(PagamentoNotFoundException.class);
        }

        @Test
        @DisplayName("ESE - Não deve atualizar pagamento já cancelado")
        void naoDeveAtualizarPagamentoCancelado() {
            var request = PagamentoTestData.criarAtualizarStatusRequest(StatusPagamento.PAGO, LocalDateTime.now());

            // Pagamento cancelado: findActiveById não o retorna (deletedAt preenchido)
            when(pagamentoRepository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pagamentoService.atualizarStatus(1L, request))
                    .isInstanceOf(PagamentoNotFoundException.class);

            verify(pagamentoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cancelamento")
    class Cancelamento {

        @Test
        @DisplayName("PCE - Deve cancelar pagamento")
        void deveCancelarPagamento() {
            Agendamento agendamento = PagamentoTestData.criarAgendamentoAtivo(50L);
            Pagamento pagamento = PagamentoTestData.criarPagamento(1L, agendamento);

            when(pagamentoRepository.findActiveById(1L)).thenReturn(Optional.of(pagamento));

            pagamentoService.cancelar(1L);

            assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.CANCELADO);
            assertThat(pagamento.getDeletedAt()).isNotNull();
            verify(pagamentoRepository).save(pagamento);
        }

        @Test
        @DisplayName("ESE - Não deve cancelar pagamento inexistente")
        void naoDeveCancelarPagamentoInexistente() {
            when(pagamentoRepository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pagamentoService.cancelar(1L))
                    .isInstanceOf(PagamentoNotFoundException.class);

            verify(pagamentoRepository, never()).save(any());
        }
    }
}
