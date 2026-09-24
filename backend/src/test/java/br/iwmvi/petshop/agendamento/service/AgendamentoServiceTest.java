package br.iwmvi.petshop.agendamento.service;

import br.iwmvi.petshop.agendamento.AgendamentoTestData;
import br.iwmvi.petshop.agendamento.dto.request.AgendamentoRequest;
import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.agendamento.model.AgendamentoStatus;
import br.iwmvi.petshop.agendamento.repository.AgendamentoRepository;
import br.iwmvi.petshop.agendamento.validator.DataHoraValidador;
import br.iwmvi.petshop.agendamento.validator.ServicosObrigatoriosValidador;
import br.iwmvi.petshop.common.dto.PaginaResponse;
import br.iwmvi.petshop.common.validator.CompositeValidator;
import br.iwmvi.petshop.exception.AgendamentoNotFoundException;
import br.iwmvi.petshop.exception.AgendamentoValidationException;
import br.iwmvi.petshop.exception.PetNotFoundException;
import br.iwmvi.petshop.pet.model.Pet;
import br.iwmvi.petshop.pet.repository.PetRepository;
import br.iwmvi.petshop.servico.model.Servico;
import br.iwmvi.petshop.servico.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private ServicoRepository servicoRepository;

    private AgendamentoService agendamentoService;

    @BeforeEach
    void setUp() {
        agendamentoService = new AgendamentoService(
                agendamentoRepository,
                petRepository,
                servicoRepository,
                new CompositeValidator<>(List.of(
                        new DataHoraValidador(),
                        new ServicosObrigatoriosValidador()
                ))
        );
    }

    @Nested
    @DisplayName("Cadastro de agendamentos")
    class Cadastro {

        @Test
        @DisplayName("PCE - Deve cadastrar agendamento válido")
        void deveCadastrarAgendamentoValido() {
            Pet pet = AgendamentoTestData.criarPet();
            AgendamentoRequest request = AgendamentoTestData.criarAgendamentoRequest(LocalDateTime.now().plusDays(1));
            List<Servico> servicos = servicosPadrao();

            when(petRepository.findActiveById(2L)).thenReturn(Optional.of(pet));
            when(servicoRepository.findByIdInAndDeletedAtIsNull(List.of(10L, 11L))).thenReturn(servicos);
            when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(i -> i.getArgument(0));

            var response = agendamentoService.cadastrar(2L, request);

            assertThat(response.petId()).isEqualTo(2L);
            assertThat(response.status()).isEqualTo(AgendamentoStatus.AGENDADO);
            assertThat(response.servicos()).hasSize(2);
            verify(agendamentoRepository).save(any(Agendamento.class));
        }

        @Test
        @DisplayName("ESE - Não deve cadastrar agendamento com pet inexistente")
        void naoDeveCadastrarComPetInexistente() {
            AgendamentoRequest request = AgendamentoTestData.criarAgendamentoRequest(LocalDateTime.now().plusDays(1));

            when(petRepository.findActiveById(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.cadastrar(2L, request))
                    .isInstanceOf(PetNotFoundException.class);

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("ESE - Não deve cadastrar agendamento com data no passado")
        void naoDeveCadastrarComDataNoPassado() {
            AgendamentoRequest request = AgendamentoTestData.criarAgendamentoRequest(LocalDateTime.now().minusMinutes(5));

            assertThatThrownBy(() -> agendamentoService.cadastrar(2L, request))
                    .isInstanceOf(AgendamentoValidationException.class)
                    .hasMessageContaining("não pode ser no passado");

            verifyNoInteractions(petRepository);
            verifyNoInteractions(servicoRepository);
        }

        @Test
        @DisplayName("ESE - Não deve cadastrar agendamento quando serviço for inválido")
        void naoDeveCadastrarQuandoServicoForInvalido() {
            Pet pet = AgendamentoTestData.criarPet();
            AgendamentoRequest request = AgendamentoTestData.criarAgendamentoRequest(LocalDateTime.now().plusDays(1));

            when(petRepository.findActiveById(2L)).thenReturn(Optional.of(pet));
            when(servicoRepository.findByIdInAndDeletedAtIsNull(List.of(10L, 11L)))
                    .thenReturn(List.of(AgendamentoTestData.criarServico(10L, "Banho", "50.00")));

            assertThatThrownBy(() -> agendamentoService.cadastrar(2L, request))
                    .isInstanceOf(AgendamentoValidationException.class)
                    .hasMessageContaining("serviços");

            verify(agendamentoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Consulta de agendamentos")
    class Consulta {

        @Test
        @DisplayName("PCE - Deve listar agendamentos do pet")
        void deveListarAgendamentosDoPet() {
            Pet pet = AgendamentoTestData.criarPet();
            Agendamento agendamento = AgendamentoTestData.criarAgendamento(pet, LocalDateTime.now().plusDays(1), servicosPadrao());

            ReflectionTestUtils.setField(agendamento, "id", 30L);

            when(petRepository.findActiveById(2L)).thenReturn(Optional.of(pet));
            when(agendamentoRepository.findIdsByPetId(eq(2L), any(Pageable.class)))
                    .thenAnswer(i -> new PageImpl<>(List.of(30L), i.getArgument(1), 11));
            when(agendamentoRepository.findAllComServicosByIdIn(List.of(30L))).thenReturn(List.of(agendamento));

            var response = agendamentoService.listarPorPet(2L, 1, 10);

            assertThat(response.itens()).hasSize(1);
            assertThat(response.itens().getFirst().petId()).isEqualTo(2L);
            assertThat(response.itens().getFirst().servicos().getFirst().nome()).isNotBlank();
            assertThat(response.pagina()).isEqualTo(1);
            assertThat(response.total()).isEqualTo(11);
            assertThat(response.totalPaginas()).isEqualTo(2);
        }

        @Test
        @DisplayName("AVL - Deve limitar o tamanho da página ao máximo permitido")
        void deveLimitarTamanhoDaPagina() {
            when(petRepository.findActiveById(2L)).thenReturn(Optional.of(AgendamentoTestData.criarPet()));
            when(agendamentoRepository.findIdsByPetId(eq(2L), any(Pageable.class)))
                    .thenAnswer(i -> new PageImpl<>(List.<Long>of(), i.getArgument(1), 0));

            agendamentoService.listarPorPet(2L, -3, 1000);

            var captor = ArgumentCaptor.forClass(Pageable.class);
            verify(agendamentoRepository).findIdsByPetId(eq(2L), captor.capture());
            assertThat(captor.getValue().getPageNumber()).isZero();
            assertThat(captor.getValue().getPageSize()).isEqualTo(PaginaResponse.TAMANHO_MAXIMO);
        }

        @Test
        @DisplayName("ESE - Não deve listar quando pet não existir")
        void naoDeveListarQuandoPetNaoExistir() {
            when(petRepository.findActiveById(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.listarPorPet(2L, 0, 10))
                    .isInstanceOf(PetNotFoundException.class);

            verifyNoInteractions(agendamentoRepository);
        }

        @Test
        @DisplayName("PCE - Deve buscar agendamento por id")
        void deveBuscarAgendamentoPorId() {
            Pet pet = AgendamentoTestData.criarPet();
            Agendamento agendamento = AgendamentoTestData.criarAgendamento(pet, LocalDateTime.now().plusDays(1), servicosPadrao());

            when(petRepository.findActiveById(2L)).thenReturn(Optional.of(pet));
            when(agendamentoRepository.findByIdAndPetIdAndDeletedAtIsNull(50L, 2L)).thenReturn(Optional.of(agendamento));

            var response = agendamentoService.buscarPorId(2L, 50L);

            assertThat(response.id()).isEqualTo(50L);
        }

        @Test
        @DisplayName("ESE - Não deve buscar agendamento inexistente")
        void naoDeveBuscarAgendamentoInexistente() {
            Pet pet = AgendamentoTestData.criarPet();

            when(petRepository.findActiveById(2L)).thenReturn(Optional.of(pet));
            when(agendamentoRepository.findByIdAndPetIdAndDeletedAtIsNull(50L, 2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.buscarPorId(2L, 50L))
                    .isInstanceOf(AgendamentoNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Atualização e cancelamento")
    class AtualizacaoECancelamento {

        @Test
        @DisplayName("PCE - Deve atualizar agendamento")
        void deveAtualizarAgendamento() {
            Pet pet = AgendamentoTestData.criarPet();
            Agendamento agendamento = AgendamentoTestData.criarAgendamento(pet, LocalDateTime.now().plusDays(1), servicosPadrao());
            AgendamentoRequest request = new AgendamentoRequest(
                    LocalDateTime.now().plusDays(2),
                    "Nova observação",
                    List.of(10L)
            );

            when(petRepository.findActiveById(2L)).thenReturn(Optional.of(pet));
            when(agendamentoRepository.findByIdAndPetIdAndDeletedAtIsNull(50L, 2L)).thenReturn(Optional.of(agendamento));
            when(servicoRepository.findByIdInAndDeletedAtIsNull(List.of(10L)))
                    .thenReturn(List.of(AgendamentoTestData.criarServico(10L, "Banho", "50.00")));
            when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(i -> i.getArgument(0));

            var response = agendamentoService.atualizar(2L, 50L, request);

            assertThat(response.observacoes()).isEqualTo("Nova observação");
            assertThat(response.servicos()).hasSize(1);
        }

        @Test
        @DisplayName("PCE - Deve cancelar agendamento")
        void deveCancelarAgendamento() {
            Pet pet = AgendamentoTestData.criarPet();
            Agendamento agendamento = AgendamentoTestData.criarAgendamento(pet, LocalDateTime.now().plusDays(1), servicosPadrao());

            when(petRepository.findActiveById(2L)).thenReturn(Optional.of(pet));
            when(agendamentoRepository.findByIdAndPetIdAndDeletedAtIsNull(50L, 2L)).thenReturn(Optional.of(agendamento));

            agendamentoService.cancelar(2L, 50L);

            assertThat(agendamento.getStatus()).isEqualTo(AgendamentoStatus.CANCELADO);
            assertThat(agendamento.getDeletedAt()).isNotNull();
            verify(agendamentoRepository).save(agendamento);
        }
    }

    private List<Servico> servicosPadrao() {
        return List.of(
                AgendamentoTestData.criarServico(10L, "Banho", "50.00"),
                AgendamentoTestData.criarServico(11L, "Tosa", "30.00")
        );
    }
}
