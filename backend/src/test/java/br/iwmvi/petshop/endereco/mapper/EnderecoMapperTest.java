package br.iwmvi.petshop.endereco.mapper;

import br.iwmvi.petshop.endereco.dto.request.EnderecoRequest;
import br.iwmvi.petshop.endereco.model.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnderecoMapperTest {

    @Nested
    @DisplayName("Conversão para entidade")
    class ConversaoParaEntidade {

        @Test
        @DisplayName("PCE - Deve remover a máscara do CEP e normalizar o estado.")
        void deveNormalizarCepEEstado() {
            var request = new EnderecoRequest(
                    "01001-010",
                    "Praça da Sé",
                    "1",
                    null,
                    "Sé",
                    "São Paulo",
                    "sp"
            );

            var endereco = EnderecoMapper.toEntity(request);

            assertThat(endereco.getCep()).isEqualTo("01001010");
            assertThat(endereco.getEstado()).isEqualTo("SP");
        }

        @Test
        @DisplayName("PCE - Deve mapear os demais campos do endereço.")
        void deveMapearOsDemaisCampos() {
            var request = new EnderecoRequest(
                    "01001-001",
                    "Praça da Sé",
                    "1",
                    "Lado Ímpar",
                    "Sé",
                    "São Paulo",
                    "SP"
            );

            var endereco = EnderecoMapper.toEntity(request);

            assertThat(endereco.getLogradouro()).isEqualTo("Praça da Sé");
            assertThat(endereco.getNumero()).isEqualTo("1");
            assertThat(endereco.getComplemento()).isEqualTo("Lado Ímpar");
            assertThat(endereco.getBairro()).isEqualTo("Sé");
            assertThat(endereco.getCidade()).isEqualTo("São Paulo");
        }
    }

    @Nested
    @DisplayName("Conversão para resposta")
    class ConversaoParaResposta {

        @Test
        @DisplayName("PCE - Deve mapear todos os campos do endereço.")
        void deveMapearTodosOsCampos() {
            var endereco = new Endereco(
                    "01001010",
                    "Praça da Sé",
                    "1",
                    "Lado Ímpar",
                    "Sé",
                    "São Paulo",
                    "SP"
            );

            var response = EnderecoMapper.toResponse(endereco);

            assertThat(response.cep()).isEqualTo("01001010");
            assertThat(response.logradouro()).isEqualTo("Praça da Sé");
            assertThat(response.numero()).isEqualTo("1");
            assertThat(response.complemento()).isEqualTo("Lado Ímpar");
            assertThat(response.bairro()).isEqualTo("Sé");
            assertThat(response.cidade()).isEqualTo("São Paulo");
            assertThat(response.estado()).isEqualTo("SP");
        }
    }
}