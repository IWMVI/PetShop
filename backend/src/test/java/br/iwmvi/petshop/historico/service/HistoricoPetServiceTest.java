package br.iwmvi.petshop.historico.service;

import br.iwmvi.petshop.exception.EntityNotFoundException;
import br.iwmvi.petshop.exception.HistoricoPetNotFoundException;
import br.iwmvi.petshop.exception.PetNotFoundException;
import br.iwmvi.petshop.funcionario.FuncionarioTestData;
import br.iwmvi.petshop.funcionario.model.Funcionario;
import br.iwmvi.petshop.funcionario.repository.FuncionarioRepository;
import br.iwmvi.petshop.historico.HistoricoPetTestData;
import br.iwmvi.petshop.historico.dto.request.HistoricoPetRequest;
import br.iwmvi.petshop.historico.mapper.HistoricoPetMapper;
import br.iwmvi.petshop.historico.model.HistoricoPet;
import br.iwmvi.petshop.historico.model.TipoEvento;
import br.iwmvi.petshop.historico.repository.HistoricoPetRepository;
import br.iwmvi.petshop.pet.PetTestData;
import br.iwmvi.petshop.pet.model.Pet;
import br.iwmvi.petshop.pet.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricoPetServiceTest {

    @Mock
    private HistoricoPetRepository historicoRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    private HistoricoPetService historicoService;

    private Pet pet;

    private Funcionario funcionario;

    @BeforeEach
    void setUp() {
        historicoService = new HistoricoPetService(
                historicoRepository, petRepository, funcionarioRepository, new HistoricoPetMapper());
        pet = PetTestData.criarPet(null);
        funcionario = FuncionarioTestData.criarFuncionario();
    }

    @Nested
    @DisplayName("Registro de eventos.")
    class RegistroEvento {

        @Test
        @DisplayName("PCE - Deve registrar evento quando o pet e o funcionário existirem.")
        void deveRegistrarEvento_quandoPetEFuncionarioExistirem() {
            var request = HistoricoPetTestData.criarHistoricoRequest();

            when(petRepository.findActiveById(1L)).thenReturn(Optional.of(pet));
            when(funcionarioRepository.findActiveById(1L)).thenReturn(Optional.of(funcionario));
            when(historicoRepository.save(any(HistoricoPet.class))).thenAnswer(i -> {
                HistoricoPet historico = i.getArgument(0);
                ReflectionTestUtils.setField(historico, "id", 1L);
                return historico;
            });

            var response = historicoService.registrar(1L, request);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.petId()).isEqualTo(1L);
            assertThat(response.tipoEvento()).isEqualTo(TipoEvento.VACINACAO);
            assertThat(response.descricao()).isEqualTo("Vacina V10 - primeira dose");
            assertThat(response.dataEvento()).isEqualTo(HistoricoPetTestData.DATA_EVENTO);
            assertThat(response.funcionarioId()).isEqualTo(1L);
            assertThat(response.funcionarioNome()).isEqualTo("Dra. Ana");

            verify(historicoRepository).save(any(HistoricoPet.class));
        }

        @Test
        @DisplayName("PCE - Deve registrar evento sem funcionário.")
        void deveRegistrarEvento_quandoSemFuncionario() {
            var request = new HistoricoPetRequest(TipoEvento.OUTRO, "Vacina aplicada em outra clínica",
                    HistoricoPetTestData.DATA_EVENTO, null);

            when(petRepository.findActiveById(1L)).thenReturn(Optional.of(pet));
            when(historicoRepository.save(any(HistoricoPet.class))).thenAnswer(i -> i.getArgument(0));

            var response = historicoService.registrar(1L, request);

            assertThat(response.funcionarioId()).isNull();
            assertThat(response.funcionarioNome()).isNull();
            verifyNoInteractions(funcionarioRepository);
        }

        @Test
        @DisplayName("ESE - Não deve registrar evento quando o pet não existir.")
        void naoDeveRegistrarEvento_quandoPetNaoExistir() {
            var request = HistoricoPetTestData.criarHistoricoRequest();

            when(petRepository.findActiveById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> historicoService.registrar(999L, request))
                    .isInstanceOf(PetNotFoundException.class);

            verify(historicoRepository, never()).save(any());
        }

        @Test
        @DisplayName("ESE - Não deve registrar evento quando o pet estiver excluído logicamente.")
        void naoDeveRegistrarEvento_quandoPetExcluido() {
            var request = HistoricoPetTestData.criarHistoricoRequest();

            when(petRepository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> historicoService.registrar(1L, request))
                    .isInstanceOf(PetNotFoundException.class)
                    .hasMessageContaining("1");

            verify(historicoRepository, never()).save(any());
        }

        @Test
        @DisplayName("ESE - Não deve registrar evento com funcionário inexistente ou desligado.")
        void naoDeveRegistrarEvento_quandoFuncionarioNaoExistir() {
            var request = new HistoricoPetRequest(TipoEvento.CONSULTA, "Consulta de rotina",
                    HistoricoPetTestData.DATA_EVENTO, 999L);

            when(petRepository.findActiveById(1L)).thenReturn(Optional.of(pet));
            when(funcionarioRepository.findActiveById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> historicoService.registrar(1L, request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Funcionário");

            verify(historicoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Listagem de eventos.")
    class ListagemEventos {

        @Test
        @DisplayName("PCE - Deve listar os eventos do pet.")
        void deveListarEventos_quandoExistirem() {
            var historico = HistoricoPetTestData.criarHistorico(pet, funcionario);

            when(petRepository.findActiveById(1L)).thenReturn(Optional.of(pet));
            when(historicoRepository.findByPetIdOrderByDataEventoDesc(1L)).thenReturn(List.of(historico));

            var response = historicoService.listarPorPet(1L);

            assertThat(response).hasSize(1);
            assertThat(response.getFirst().id()).isEqualTo(1L);
            assertThat(response.getFirst().funcionarioNome()).isEqualTo("Dra. Ana");
        }

        @Test
        @DisplayName("PCE - Deve manter o funcionário no histórico mesmo após ser desligado.")
        void deveManterFuncionarioNoHistorico_quandoDesligado() {
            funcionario.delete();
            var historico = HistoricoPetTestData.criarHistorico(pet, funcionario);

            when(petRepository.findActiveById(1L)).thenReturn(Optional.of(pet));
            when(historicoRepository.findByPetIdOrderByDataEventoDesc(1L)).thenReturn(List.of(historico));

            var response = historicoService.listarPorPet(1L);

            assertThat(response.getFirst().funcionarioNome()).isEqualTo("Dra. Ana");
        }

        @Test
        @DisplayName("PCE - Deve retornar lista vazia quando o pet não possuir eventos.")
        void deveRetornarListaVazia_quandoNaoExistiremEventos() {
            when(petRepository.findActiveById(1L)).thenReturn(Optional.of(pet));
            when(historicoRepository.findByPetIdOrderByDataEventoDesc(1L)).thenReturn(List.of());

            assertThat(historicoService.listarPorPet(1L)).isEmpty();
        }

        @Test
        @DisplayName("ESE - Não deve listar eventos quando o pet não existir.")
        void naoDeveListarEventos_quandoPetNaoExistir() {
            when(petRepository.findActiveById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> historicoService.listarPorPet(999L))
                    .isInstanceOf(PetNotFoundException.class);

            verify(historicoRepository, never()).findByPetIdOrderByDataEventoDesc(any());
        }
    }

    @Nested
    @DisplayName("Consulta de evento por id.")
    class ConsultaEvento {

        @Test
        @DisplayName("PCE - Deve consultar evento quando o id pertencer ao pet.")
        void deveConsultarEvento_quandoIdPertencerAoPet() {
            var historico = HistoricoPetTestData.criarHistorico(pet, funcionario);

            when(petRepository.findActiveById(1L)).thenReturn(Optional.of(pet));
            when(historicoRepository.findByIdAndPetId(1L, 1L)).thenReturn(Optional.of(historico));

            var response = historicoService.buscarPorId(1L, 1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.tipoEvento()).isEqualTo(TipoEvento.VACINACAO);
        }

        @Test
        @DisplayName("ESE - Não deve consultar evento quando o id não existir.")
        void naoDeveConsultarEvento_quandoIdNaoExistir() {
            when(petRepository.findActiveById(1L)).thenReturn(Optional.of(pet));
            when(historicoRepository.findByIdAndPetId(999L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> historicoService.buscarPorId(1L, 999L))
                    .isInstanceOf(HistoricoPetNotFoundException.class);
        }

        @Test
        @DisplayName("ESE - Não deve consultar evento de outro pet.")
        void naoDeveConsultarEvento_quandoPertencerAOutroPet() {
            when(petRepository.findActiveById(1L)).thenReturn(Optional.of(pet));
            when(historicoRepository.findByIdAndPetId(5L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> historicoService.buscarPorId(1L, 5L))
                    .isInstanceOf(HistoricoPetNotFoundException.class);
        }

        @Test
        @DisplayName("ESE - Não deve consultar evento quando o pet não existir.")
        void naoDeveConsultarEvento_quandoPetNaoExistir() {
            when(petRepository.findActiveById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> historicoService.buscarPorId(999L, 1L))
                    .isInstanceOf(PetNotFoundException.class);

            verify(historicoRepository, never()).findByIdAndPetId(any(), any());
        }
    }
}
