package br.iwmvi.petshop.funcionario.service;

import br.iwmvi.petshop.exception.CpfJaCadastradoException;
import br.iwmvi.petshop.exception.EntityNotFoundException;
import br.iwmvi.petshop.funcionario.FuncionarioTestData;
import br.iwmvi.petshop.funcionario.dto.request.FuncionarioRequest;
import br.iwmvi.petshop.funcionario.mapper.FuncionarioMapper;
import br.iwmvi.petshop.funcionario.model.Cargo;
import br.iwmvi.petshop.funcionario.model.Funcionario;
import br.iwmvi.petshop.funcionario.repository.FuncionarioRepository;
import br.iwmvi.petshop.tutor.CpfTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    private static final String OUTRO_CPF = "11144477735";

    @Mock
    private FuncionarioRepository repository;

    private FuncionarioService service;

    @BeforeEach
    void setUp() {
        service = new FuncionarioService(repository, new FuncionarioMapper());
    }

    @Nested
    @DisplayName("Cadastro de funcionários.")
    class Cadastro {

        @Test
        @DisplayName("PCE - Deve cadastrar funcionário quando o CPF não estiver em uso.")
        void deveCadastrarFuncionario_quandoCpfLivre() {
            when(repository.existsByCpf(CpfTestData.VALIDO)).thenReturn(false);
            when(repository.save(any(Funcionario.class))).thenAnswer(i -> {
                Funcionario funcionario = i.getArgument(0);
                ReflectionTestUtils.setField(funcionario, "id", 1L);
                return funcionario;
            });

            var response = service.create(FuncionarioTestData.criarFuncionarioRequest());

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.nome()).isEqualTo("Dra. Ana");
            assertThat(response.cargo()).isEqualTo(Cargo.VETERINARIO);
        }

        @Test
        @DisplayName("PCE - Deve armazenar o CPF apenas com dígitos.")
        void deveArmazenarCpfApenasComDigitos() {
            var request = new FuncionarioRequest("Dra. Ana", CpfTestData.VALIDO_FORMATADO, Cargo.VETERINARIO, "11988887777");

            when(repository.existsByCpf(CpfTestData.VALIDO)).thenReturn(false);
            when(repository.save(any(Funcionario.class))).thenAnswer(i -> i.getArgument(0));

            var response = service.create(request);

            assertThat(response.cpf()).isEqualTo(CpfTestData.VALIDO);
        }

        @Test
        @DisplayName("ESE - Não deve cadastrar funcionário com CPF já cadastrado.")
        void naoDeveCadastrarFuncionario_quandoCpfJaCadastrado() {
            when(repository.existsByCpf(CpfTestData.VALIDO)).thenReturn(true);

            assertThatThrownBy(() -> service.create(FuncionarioTestData.criarFuncionarioRequest()))
                    .isInstanceOf(CpfJaCadastradoException.class);

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Atualização de funcionários.")
    class Atualizacao {

        @Test
        @DisplayName("PCE - Deve atualizar funcionário mantendo o próprio CPF.")
        void deveAtualizarFuncionario_quandoCpfNaoMudar() {
            var funcionario = FuncionarioTestData.criarFuncionario();
            var request = new FuncionarioRequest("Dra. Ana Souza", CpfTestData.VALIDO, Cargo.GERENTE, "11988887777");

            when(repository.findActiveById(1L)).thenReturn(Optional.of(funcionario));
            when(repository.save(any(Funcionario.class))).thenAnswer(i -> i.getArgument(0));

            var response = service.update(1L, request);

            assertThat(response.nome()).isEqualTo("Dra. Ana Souza");
            assertThat(response.cargo()).isEqualTo(Cargo.GERENTE);
            verify(repository, never()).existsByCpf(any());
        }

        @Test
        @DisplayName("ESE - Não deve atualizar para um CPF de outro funcionário.")
        void naoDeveAtualizarFuncionario_quandoCpfPertencerAOutro() {
            var request = new FuncionarioRequest("Dra. Ana", OUTRO_CPF, Cargo.VETERINARIO, "11988887777");

            when(repository.findActiveById(1L)).thenReturn(Optional.of(FuncionarioTestData.criarFuncionario()));
            when(repository.existsByCpf(OUTRO_CPF)).thenReturn(true);

            assertThatThrownBy(() -> service.update(1L, request))
                    .isInstanceOf(CpfJaCadastradoException.class);

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Busca e exclusão de funcionários.")
    class BuscaExclusao {

        @Test
        @DisplayName("ESE - Não deve buscar funcionário inexistente.")
        void naoDeveBuscarFuncionario_quandoNaoExistir() {
            when(repository.findActiveById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Funcionário");
        }

        @Test
        @DisplayName("PCE - Deve excluir funcionário logicamente.")
        void deveExcluirFuncionarioLogicamente() {
            when(repository.findActiveById(1L)).thenReturn(Optional.of(FuncionarioTestData.criarFuncionario()));

            service.delete(1L);

            verify(repository).softDelete(1L);
        }
    }
}
