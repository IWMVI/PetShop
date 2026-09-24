package br.iwmvi.petshop.pet.mapper;

import br.iwmvi.petshop.tutor.CpfTestData;
import br.iwmvi.petshop.endereco.model.Endereco;
import br.iwmvi.petshop.pet.dto.request.PetRequest;
import br.iwmvi.petshop.pet.PetTestData;
import br.iwmvi.petshop.tutor.model.Tutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PetMapperTest {

    @Nested
    @DisplayName("Conversão para entidade")
    class ConversaoParaEntidade {

        @Test
        @DisplayName("PCE - Deve mapear todos os campos do pet.")
        void deveMapearTodosOsCampos() {
            var request = new PetRequest(
                    "Fluffy",
                    "Gato",
                    "Persa",
                    2,
                    new BigDecimal("5.50")
            );

            var tutor = criarTutor();
            var pet = new PetMapper().toEntity(request, tutor);

            assertThat(pet.getNome()).isEqualTo("Fluffy");
            assertThat(pet.getEspecie()).isEqualTo("Gato");
            assertThat(pet.getRaca()).isEqualTo("Persa");
            assertThat(pet.getIdade()).isEqualTo(2);
            assertThat(pet.getPeso()).isEqualTo(new BigDecimal("5.50"));
            assertThat(pet.getTutor()).isEqualTo(tutor);
        }
    }

    @Nested
    @DisplayName("Conversão para resposta")
    class ConversaoParaResposta {

        @Test
        @DisplayName("PCE - Deve mapear todos os campos do pet para a resposta.")
        void deveMapearTodosOsCamposParaResposta() {
            var tutor = criarTutor();
            var pet = PetTestData.criarPet(tutor);

            var response = new PetMapper().toResponse(pet);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.nome()).isEqualTo("Fluffy");
            assertThat(response.especie()).isEqualTo("Gato");
            assertThat(response.raca()).isEqualTo("Persa");
            assertThat(response.idade()).isEqualTo(2);
            assertThat(response.peso()).isEqualTo(new BigDecimal("5.50"));
        }
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
