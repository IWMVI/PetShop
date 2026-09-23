package br.iwmvi.petshop.tutor.mapper;

import br.iwmvi.petshop.endereco.dto.request.EnderecoRequest;
import br.iwmvi.petshop.tutor.dto.request.TutorRequest;
import br.iwmvi.petshop.tutor.model.Tutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class TutorMapperTest {

    @Nested
    @DisplayName("Conversão para entidade")
    class ConversaoParaEntidade {

        @Test
        @DisplayName("PCE - Deve normalizar e-mail, telefone, CEP e estado.")
        void deveNormalizarCampos() {
            var request = new TutorRequest(
                    "Wallace",
                    "WALLACE@Test.Com",
                    "(11) 91111-2222",
                    new EnderecoRequest(
                            "01001-010",
                            "Praça da Sé",
                            "1",
                            null,
                            "Sé",
                            "São Paulo",
                            "sp"
                    )
            );

            var tutor = new TutorMapper().toEntity(request);

            assertThat(tutor.getNome()).isEqualTo("Wallace");
            assertThat(tutor.getEmail()).isEqualTo("wallace@test.com");
            assertThat(tutor.getTelefone()).isEqualTo("11911112222");
            assertThat(tutor.getEndereco().getCep()).isEqualTo("01001010");
            assertThat(tutor.getEndereco().getEstado()).isEqualTo("SP");
        }
    }

    @Nested
    @DisplayName("Conversão para resposta")
    class ConversaoParaResposta {

        @Test
        @DisplayName("PCE - Deve mapear todos os campos do tutor.")
        void deveMapearTodosOsCampos() {
            var tutor = new TutorMapper().toEntity(new TutorRequest(
                    "Wallace",
                    "wallace@test.com",
                    "11911112222",
                    new EnderecoRequest(
                            "01001010",
                            "Praça da Sé",
                            "1",
                            null,
                            "Sé",
                            "São Paulo",
                            "SP"
                    )
            ));

            ReflectionTestUtils.setField(tutor, "id", 1L);

            var response = new TutorMapper().toResponse(tutor);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.nome()).isEqualTo("Wallace");
            assertThat(response.email()).isEqualTo("wallace@test.com");
            assertThat(response.telefone()).isEqualTo("11911112222");
            assertThat(response.endereco()).isNotNull();
            assertThat(response.endereco().cep()).isEqualTo("01001010");
            assertThat(response.endereco().estado()).isEqualTo("SP");
        }
    }
}