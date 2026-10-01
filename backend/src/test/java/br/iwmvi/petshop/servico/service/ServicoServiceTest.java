package br.iwmvi.petshop.servico.service;

import br.iwmvi.petshop.exception.EntityNotFoundException;
import br.iwmvi.petshop.servico.ServicoTestData;
import br.iwmvi.petshop.servico.dto.request.ServicoRequest;
import br.iwmvi.petshop.servico.mapper.ServicoMapper;
import br.iwmvi.petshop.servico.model.Servico;
import br.iwmvi.petshop.servico.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServicoServiceTest {

    @Mock
    private ServicoRepository repository;

    private ServicoService servicoService;

    @BeforeEach
    void setUp() {
        servicoService = new ServicoService(repository, new ServicoMapper());
    }

    @Nested
    @DisplayName("Cadastro de serviços.")
    class CadastroServico {

        @Test
        @DisplayName("PCE - Deve cadastrar serviço com dados válidos.")
        void deveCadastrarServico_quandoDadosValidos() {
            var request = ServicoTestData.criarServicoRequest();

            when(repository.save(any(Servico.class))).thenAnswer(i -> {
                Servico servico = i.getArgument(0);
                ReflectionTestUtils.setField(servico, "id", 1L);
                return servico;
            });

            var response = servicoService.cadastrar(request);

            assertThat(response).isNotNull();
            assertThat(response.nome()).isEqualTo("Banho e Tosa");
            assertThat(response.preco()).isEqualTo(new BigDecimal("150.00"));
            assertThat(response.tempoEstimadoMinutos()).isEqualTo(60);

            verify(repository).save(any(Servico.class));
        }
    }

    @Nested
    @DisplayName("Listagem de serviços.")
    class ListagemServicos {

        @Test
        @DisplayName("PCE - Deve listar todos os serviços.")
        void deveListarServicos_quandoExistirem() {
            var servico = ServicoTestData.criarServico();

            when(repository.findAllActive()).thenReturn(List.of(servico));

            var response = servicoService.findAll();

            assertThat(response).hasSize(1);
            assertThat(response.getFirst().id()).isEqualTo(1L);
            assertThat(response.getFirst().nome()).isEqualTo("Banho e Tosa");
        }

        @Test
        @DisplayName("PCE - Deve retornar lista vazia quando não há serviços.")
        void deveRetornarListaVazia_quandoNaoExistiremServicos() {
            when(repository.findAllActive()).thenReturn(List.of());

            var response = servicoService.findAll();

            assertThat(response).isEmpty();
        }

        @Test
        @DisplayName("ESE - Não deve listar serviços excluídos logicamente.")
        void naoDeveListarServicos_quandoExcluidosLogicamente() {
            when(repository.findAllActive()).thenReturn(List.of());

            var response = servicoService.findAll();

            assertThat(response).isEmpty();
            verify(repository).findAllActive();
        }
    }

    @Nested
    @DisplayName("Busca de serviço por id.")
    class BuscaServico {

        @Test
        @DisplayName("PCE - Deve buscar serviço quando o id existir.")
        void deveBuscarServico_quandoIdExistir() {
            var servico = ServicoTestData.criarServico();

            when(repository.findActiveById(1L)).thenReturn(Optional.of(servico));

            var response = servicoService.buscarPorId(1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.nome()).isEqualTo("Banho e Tosa");
        }

        @Test
        @DisplayName("ESE - Não deve buscar serviço quando o id não existir.")
        void naoDeveBuscarServico_quandoIdNaoExistir() {
            when(repository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> servicoService.buscarPorId(1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("ESE - Não deve buscar serviço excluído logicamente.")
        void naoDeveBuscarServico_quandoExcluidoLogicamente() {
            when(repository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> servicoService.buscarPorId(1L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("1");
        }
    }

    @Nested
    @DisplayName("Atualização de serviços.")
    class AtualizacaoServico {

        @Test
        @DisplayName("PCE - Deve atualizar serviço quando o id existir.")
        void deveAtualizarServico_quandoIdExistir() {
            var servico = ServicoTestData.criarServico();
            var request = new ServicoRequest(
                    "Banho Premium",
                    "Banho com produtos premium",
                    new BigDecimal("200.00"),
                    90
            );

            when(repository.findActiveById(1L)).thenReturn(Optional.of(servico));
            when(repository.save(any(Servico.class))).thenAnswer(i -> i.getArgument(0));

            var response = servicoService.atualizar(1L, request);

            assertThat(response.nome()).isEqualTo("Banho Premium");
            assertThat(response.preco()).isEqualTo(new BigDecimal("200.00"));
            assertThat(servico.getNome()).isEqualTo("Banho Premium");

            verify(repository).save(servico);
        }

        @Test
        @DisplayName("ESE - Não deve atualizar serviço quando o id não existir.")
        void naoDeveAtualizarServico_quandoIdNaoExistir() {
            var request = ServicoTestData.criarServicoRequest();

            when(repository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> servicoService.atualizar(1L, request))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Exclusão de serviços.")
    class ExclusaoServico {

        @Test
        @DisplayName("PCE - Deve marcar o serviço como excluído quando o id existir.")
        void deveExcluirServico_quandoIdExistir() {
            var servico = ServicoTestData.criarServico();

            when(repository.findActiveById(1L)).thenReturn(Optional.of(servico));

            servicoService.deletar(1L);

            verify(repository).softDelete(1L);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("ESE - Não deve excluir serviço quando o id não existir.")
        void naoDeveExcluirServico_quandoIdNaoExistir() {
            when(repository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> servicoService.deletar(1L))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(repository, never()).save(any());
        }
    }
}
