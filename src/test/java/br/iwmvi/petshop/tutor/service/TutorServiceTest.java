package br.iwmvi.petshop.tutor.service;

import br.iwmvi.petshop.endereco.dto.request.EnderecoRequest;
import br.iwmvi.petshop.tutor.dto.request.TutorRequest;
import br.iwmvi.petshop.exception.EmailJaCadastradoException;
import br.iwmvi.petshop.tutor.model.Tutor;
import br.iwmvi.petshop.tutor.repository.TutorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TutorServiceTest {

    @Mock
    private TutorRepository tutorRepository;

    @InjectMocks
    private TutorService tutorService;

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
            assertThat(response.telefone()).isEqualTo("11111111111");
            assertThat(response.endereco()).isNotNull();

            verify(tutorRepository).existsByEmail("wallace@test.com");
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
        @DisplayName("PCE - Deve normalizar o e-mail antes de verificar duplicidade.")
        void deveNormalizarEmail_quandoEmailPossuiLetrasMaiusculas() {
            var request = new TutorRequest(
                    "wallace",
                    "WALLACE@TEST.COM",
                    "11111111111",
                    criarEnderecoRequest()
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
                    "WALLACE@test.com",
                    "11111111111",
                    new EnderecoRequest(
                            "01001-010",
                            "Praça da Sé",
                            "01",
                            "Lado Ímpar",
                            "Sé",
                            "São Paulo",
                            "sp"
                    )
            );

            when(tutorRepository.existsByEmail("wallace@test.com")).thenReturn(false);
            when(tutorRepository.save(any(Tutor.class))).thenAnswer(i -> i.getArgument(0));
            tutorService.cadastrar(request);

            var captor = ArgumentCaptor.forClass(Tutor.class);
            verify(tutorRepository).save(captor.capture());

            Tutor tutorPersistido = captor.getValue();

            assertThat(tutorPersistido.getEmail()).isEqualTo("wallace@test.com");
            assertThat(tutorPersistido.getEndereco().getCep()).isEqualTo("01001010");
            assertThat(tutorPersistido.getEndereco().getEstado()).isEqualTo("SP");
        }
    }

    private TutorRequest criarTutorRequest() {
        return new TutorRequest(
                "Wallace",
                "wallace@test.com",
                "11111111111",
                criarEnderecoRequest()
        );
    }

    private EnderecoRequest criarEnderecoRequest() {
        return new EnderecoRequest(
                "01001-001",
                "Praça da Sé",
                "1",
                "Lado Ímpar",
                "Sé",
                "São Paulo",
                "SP"
        );
    }
}
