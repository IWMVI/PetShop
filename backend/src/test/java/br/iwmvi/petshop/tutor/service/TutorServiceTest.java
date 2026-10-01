package br.iwmvi.petshop.tutor.service;

import br.iwmvi.petshop.tutor.CpfTestData;
import br.iwmvi.petshop.endereco.EnderecoTestData;
import br.iwmvi.petshop.endereco.model.Endereco;
import br.iwmvi.petshop.exception.CpfJaCadastradoException;
import br.iwmvi.petshop.exception.EmailJaCadastradoException;
import br.iwmvi.petshop.exception.EntityNotFoundException;
import br.iwmvi.petshop.exception.TutorNotFoundException;
import br.iwmvi.petshop.tutor.dto.request.TutorRequest;
import br.iwmvi.petshop.tutor.mapper.TutorMapper;
import br.iwmvi.petshop.tutor.model.Tutor;
import br.iwmvi.petshop.tutor.repository.TutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TutorServiceTest {

    @Mock
    private TutorRepository tutorRepository;

    private TutorService tutorService;

    @BeforeEach
    void setUp() {
        tutorService = new TutorService(tutorRepository, new TutorMapper());
    }

    @Nested
    @DisplayName("Cadastro de tutores.")
    class CadastroTutor {

        @Test
        @DisplayName("PCE - Deve cadastrar tutor quando o e-mail não ainda estiver cadastrado.")
        void deveCadastrarTutor_quandoEmailNaoEstiverCadastrado() {
            var request = criarTutorRequest();

            when(tutorRepository.existsByEmail("wallace@test.com")).thenReturn(false);

            when(tutorRepository.save(any(Tutor.class))).thenAnswer(i -> i.getArgument(0));

            var response = tutorService.cadastrar(request);

            assertThat(response).isNotNull();
            assertThat(response.nome()).isEqualTo("Wallace");
            assertThat(response.email()).isEqualTo("wallace@test.com");
            assertThat(response.cpf()).isEqualTo(CpfTestData.VALIDO);
            assertThat(response.telefone()).isEqualTo("11111111111");
            assertThat(response.endereco()).isNotNull();

            verify(tutorRepository).existsByEmail("wallace@test.com");
            verify(tutorRepository).existsByCpf(CpfTestData.VALIDO);
            verify(tutorRepository).save(any(Tutor.class));
            verifyNoMoreInteractions(tutorRepository);
        }

        @Test
        @DisplayName("PCE - Não deve cadastrar tutor quando o e-mail já estiver cadastrado.")
        void naoDeveCadastrarTutor_quandoEmailJaEstiverCadastrado() {
            var request = criarTutorRequest();

            when(tutorRepository.existsByEmail("wallace@test.com")).thenReturn(true);

            assertThatThrownBy(() -> tutorService.cadastrar(request)).isInstanceOf(EmailJaCadastradoException.class).
                    hasMessageContaining("wallace@test.com");

            verify(tutorRepository).existsByEmail("wallace@test.com");
            verify(tutorRepository, never()).save(any());
            verifyNoMoreInteractions(tutorRepository);
        }

        @Test
        @DisplayName("PCE - Não deve cadastrar tutor quando o CPF já estiver cadastrado.")
        void naoDeveCadastrarTutor_quandoCpfJaEstiverCadastrado() {
            var request = new TutorRequest(
                    "Wallace",
                    CpfTestData.VALIDO_FORMATADO,
                    "wallace@test.com",
                    "11111111111",
                    EnderecoTestData.criarEnderecoRequest()
            );

            when(tutorRepository.existsByEmail("wallace@test.com")).thenReturn(false);
            when(tutorRepository.existsByCpf(CpfTestData.VALIDO)).thenReturn(true);

            assertThatThrownBy(() -> tutorService.cadastrar(request))
                    .isInstanceOf(CpfJaCadastradoException.class)
                    .hasMessage("CPF já cadastrado.");

            verify(tutorRepository, never()).save(any());
        }

        @Test
        @DisplayName("PCE - Deve normalizar o e-mail antes de verificar duplicidade.")
        void deveNormalizarEmail_quandoEmailPossuiLetrasMaiusculas() {
            var request = new TutorRequest(
                    "wallace",
                    CpfTestData.VALIDO,
                    "WALLACE@TEST.COM",
                    "11111111111",
                    EnderecoTestData.criarEnderecoRequest()
            );

            when(tutorRepository.existsByEmail("wallace@test.com")).thenReturn(false);
            when(tutorRepository.save(any(Tutor.class))).thenAnswer(i -> i.getArgument(0));
            tutorService.cadastrar(request);

            verify(tutorRepository).existsByEmail("wallace@test.com");
        }

        @Test
        @DisplayName("PCE - Deve persistir o tutor com os dados normalizados.")
        void devePersistirTutorComDadosNormalizados_quandoCadastroForValido() {
            var request = new TutorRequest(
                    "Wallace",
                    CpfTestData.VALIDO,
                    "WALLACE@test.com",
                    "11111111111",
                    EnderecoTestData.criarEnderecoRequest()
            );

            when(tutorRepository.existsByEmail("wallace@test.com")).thenReturn(false);
            when(tutorRepository.save(any(Tutor.class))).thenAnswer(i -> i.getArgument(0));
            tutorService.cadastrar(request);

            var captor = ArgumentCaptor.forClass(Tutor.class);
            verify(tutorRepository).save(captor.capture());

            Tutor tutorPersistido = captor.getValue();

            assertThat(tutorPersistido.getEmail()).isEqualTo("wallace@test.com");
        }
    }

    @Nested
    @DisplayName("Listagem de tutores.")
    class ListagemTutores {

        @Test
        @DisplayName("PCE - Deve listar os tutores cadastrados.")
        void deveListarTutores_quandoExistirem() {
            var tutor = criarTutor();

            when(tutorRepository.findAllActive()).thenReturn(List.of(tutor));

            var response = tutorService.findAll();

            assertThat(response).hasSize(1);
            assertThat(response.getFirst().id()).isEqualTo(1L);
            assertThat(response.getFirst().nome()).isEqualTo("Wallace");
            assertThat(response.getFirst().email()).isEqualTo("wallace@test.com");
        }

        @Test
        @DisplayName("PCE - Deve retornar lista vazia quando não houver tutores cadastrados.")
        void deveRetornarListaVazia_quandoNaoExistiremTutores() {
            when(tutorRepository.findAllActive()).thenReturn(List.of());

            var response = tutorService.findAll();

            assertThat(response).isEmpty();
        }

        @Test
        @DisplayName("ESE - Não deve listar tutores excluídos logicamente.")
        void naoDeveListarTutores_quandoExcluidosLogicamente() {
            when(tutorRepository.findAllActive()).thenReturn(List.of());

            var response = tutorService.findAll();

            assertThat(response).isEmpty();
            verify(tutorRepository).findAllActive();
            verify(tutorRepository, never()).findAll();
        }
    }

    @Nested
    @DisplayName("Busca de tutor por id.")
    class BuscaTutor {

        @Test
        @DisplayName("PCE - Deve buscar tutor quando o id existir.")
        void deveBuscarTutor_quandoIdExistir() {
            var tutor = criarTutor();

            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.of(tutor));

            var response = tutorService.buscarPorId(1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.nome()).isEqualTo("Wallace");
            assertThat(response.email()).isEqualTo("wallace@test.com");
        }

        @Test
        @DisplayName("ESE - Não deve buscar tutor quando o id não existir.")
        void naoDeveBuscarTutor_quandoIdNaoExistir() {
            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tutorService.buscarPorId(1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("ESE - Não deve buscar tutor excluído logicamente.")
        void naoDeveBuscarTutor_quandoExcluidoLogicamente() {
            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tutorService.buscarPorId(1L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("1");
        }
    }

    @Nested
    @DisplayName("Atualização de tutores.")
    class AtualizacaoTutor {

        @Test
        @DisplayName("PCE - Deve atualizar tutor quando o id existir e o e-mail não estiver em uso.")
        void deveAtualizarTutor_quandoDadosForemValidos() {
            var tutor = criarTutor();
            var request = new TutorRequest(
                    "Wallace Atualizado",
                    CpfTestData.VALIDO,
                    "NOVO@test.com",
                    "11 91111-2222",
                    EnderecoTestData.criarEnderecoRequest()
            );

            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.of(tutor));
            when(tutorRepository.existsByEmail("novo@test.com")).thenReturn(false);
            when(tutorRepository.save(any(Tutor.class))).thenAnswer(i -> i.getArgument(0));

            var response = tutorService.atualizar(1L, request);

            assertThat(response.nome()).isEqualTo("Wallace Atualizado");
            assertThat(response.email()).isEqualTo("novo@test.com");
            assertThat(response.telefone()).isEqualTo("11911112222");
            assertThat(tutor.getNome()).isEqualTo("Wallace Atualizado");
            assertThat(tutor.getEmail()).isEqualTo("novo@test.com");
            assertThat(tutor.getTelefone()).isEqualTo("11911112222");

            verify(tutorRepository).save(tutor);
        }

        @Test
        @DisplayName("ESE - Não deve atualizar tutor quando o id não existir.")
        void naoDeveAtualizarTutor_quandoIdNaoExistir() {
            var request = new TutorRequest(
                    "Wallace",
                    CpfTestData.VALIDO,
                    "wallace@test.com",
                    "11111111111",
                    EnderecoTestData.criarEnderecoRequest()
            );

            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tutorService.atualizar(1L, request))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(tutorRepository, never()).save(any());
        }

        @Test
        @DisplayName("ESE - Não deve atualizar tutor quando o e-mail já estiver em uso por outro tutor.")
        void naoDeveAtualizarTutor_quandoEmailJaEstiverEmUso() {
            var tutor = criarTutor();
            var request = new TutorRequest(
                    "Wallace",
                    CpfTestData.VALIDO,
                    "outro@test.com",
                    "11111111111",
                    EnderecoTestData.criarEnderecoRequest()
            );

            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.of(tutor));
            when(tutorRepository.existsByEmail("outro@test.com")).thenReturn(true);

            assertThatThrownBy(() -> tutorService.atualizar(1L, request))
                    .isInstanceOf(EmailJaCadastradoException.class)
                    .hasMessageContaining("outro@test.com");

            verify(tutorRepository, never()).save(tutor);
        }

        @Test
        @DisplayName("ESE - Não deve atualizar tutor quando o CPF já estiver em uso por outro tutor.")
        void naoDeveAtualizarTutor_quandoCpfJaEstiverEmUso() {
            var tutor = criarTutor();
            var outroCpf = CpfTestData.gerar();
            var request = new TutorRequest(
                    "Wallace",
                    outroCpf,
                    "wallace@test.com",
                    "11111111111",
                    EnderecoTestData.criarEnderecoRequest()
            );

            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.of(tutor));
            when(tutorRepository.existsByCpf(outroCpf)).thenReturn(true);

            assertThatThrownBy(() -> tutorService.atualizar(1L, request))
                    .isInstanceOf(CpfJaCadastradoException.class);

            verify(tutorRepository, never()).save(any());
        }

        @Test
        @DisplayName("AVL - Deve permitir atualizar quando o e-mail se mantém o mesmo do tutor.")
        void devePermitirAtualizar_quandoEmailSeManterDoMesmoTutor() {
            var tutor = criarTutor();
            var request = new TutorRequest(
                    "Wallace Atualizado",
                    CpfTestData.VALIDO,
                    "wallace@test.com",
                    "11911112222",
                    EnderecoTestData.criarEnderecoRequest()
            );

            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.of(tutor));
            when(tutorRepository.save(any(Tutor.class))).thenAnswer(i -> i.getArgument(0));

            var response = tutorService.atualizar(1L, request);

            assertThat(response.nome()).isEqualTo("Wallace Atualizado");

            verify(tutorRepository).save(tutor);
            verify(tutorRepository, never()).existsByEmail("wallace@test.com");
        }
    }

    @Nested
    @DisplayName("Exclusão de tutores.")
    class ExclusaoTutor {

        @Test
        @DisplayName("PCE - Deve marcar o tutor como excluído quando o id existir.")
        void deveExcluirTutor_quandoIdExistir() {
            var tutor = criarTutor();

            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.of(tutor));

            tutorService.excluir(1L);

            verify(tutorRepository).softDelete(1L);
            verify(tutorRepository, never()).save(any());
        }

        @Test
        @DisplayName("ESE - Não deve excluir tutor quando o id não existir.")
        void naoDeveExcluirTutor_quandoIdNaoExistir() {
            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tutorService.excluir(1L))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(tutorRepository, never()).softDelete(any());
            verify(tutorRepository, never()).save(any());
        }

        @Test
        @DisplayName("ESE - Não deve excluir novamente um tutor já excluído logicamente.")
        void naoDeveExcluirTutor_quandoJaExcluidoLogicamente() {
            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tutorService.excluir(1L))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(tutorRepository).findActiveById(1L);
            verify(tutorRepository, never()).findById(1L);
            verify(tutorRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Restauração de tutores.")
    class RestauracaoTutor {

        @Test
        @DisplayName("PCE - Deve restaurar tutor excluído quando o id existir.")
        void deveRestaurarTutor_quandoIdExistir() {
            var tutor = criarTutor();
            tutor.setDeletedAt(LocalDateTime.now());

            when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutor));

            var response = tutorService.restaurar(1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(tutor.getDeletedAt()).isNull();
            verify(tutorRepository).save(tutor);
        }

        @Test
        @DisplayName("ESE - Não deve restaurar tutor quando o id não existir.")
        void naoDeveRestaurarTutor_quandoIdNaoExistir() {
            when(tutorRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tutorService.restaurar(1L))
                    .isInstanceOf(TutorNotFoundException.class);

            verify(tutorRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Busca de tutor por CPF.")
    class BuscaPorCpf {

        @Test
        @DisplayName("PCE - Deve encontrar tutor excluído pelo CPF com máscara, sinalizando a exclusão.")
        void deveEncontrarTutorExcluido_quandoCpfPertencerAoTutor() {
            var tutor = criarTutor();
            tutor.setDeletedAt(LocalDateTime.now());

            when(tutorRepository.findByCpf(CpfTestData.VALIDO)).thenReturn(Optional.of(tutor));

            var resumo = tutorService.buscarPorCpf(CpfTestData.VALIDO_FORMATADO);

            assertThat(resumo.id()).isEqualTo(1L);
            assertThat(resumo.nome()).isEqualTo("Wallace");
            assertThat(resumo.email()).isEqualTo("wallace@test.com");
            assertThat(resumo.excluido()).isTrue();
        }

        @Test
        @DisplayName("PCE - Deve indicar que o tutor está ativo.")
        void deveIndicarTutorAtivo_quandoNaoExcluido() {
            when(tutorRepository.findByCpf(CpfTestData.VALIDO)).thenReturn(Optional.of(criarTutor()));

            assertThat(tutorService.buscarPorCpf(CpfTestData.VALIDO).excluido()).isFalse();
        }

        @Test
        @DisplayName("ESE - Deve lançar exceção quando nenhum tutor tiver o CPF.")
        void deveLancarExcecao_quandoCpfNaoEncontrado() {
            when(tutorRepository.findByCpf(CpfTestData.VALIDO)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tutorService.buscarPorCpf(CpfTestData.VALIDO))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    private TutorRequest criarTutorRequest() {
        return new TutorRequest(
                "Wallace",
                CpfTestData.VALIDO,
                "wallace@test.com",
                "11111111111",
                EnderecoTestData.criarEnderecoRequest()
        );
    }

    private Tutor criarTutor() {
        var tutor = new Tutor(
                "Wallace",
                CpfTestData.VALIDO,
                "wallace@test.com",
                "11999999999",
                new Endereco(
                        "01001010",
                        "Praça da Sé",
                        "1",
                        null,
                        "Sé",
                        "São Paulo",
                        "SP"
                )
        );

        ReflectionTestUtils.setField(tutor, "id", 1L);

        return tutor;
    }
}