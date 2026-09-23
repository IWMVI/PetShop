package br.iwmvi.petshop.pet.service;

import br.iwmvi.petshop.endereco.model.Endereco;
import br.iwmvi.petshop.exception.PetNotFoundException;
import br.iwmvi.petshop.exception.TutorNotFoundException;
import br.iwmvi.petshop.pet.PetTestData;
import br.iwmvi.petshop.pet.dto.request.PetRequest;
import br.iwmvi.petshop.pet.mapper.PetMapper;
import br.iwmvi.petshop.pet.model.Pet;
import br.iwmvi.petshop.pet.repository.PetRepository;
import br.iwmvi.petshop.tutor.model.Tutor;
import br.iwmvi.petshop.tutor.repository.TutorRepository;
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
public class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private TutorRepository tutorRepository;

    private PetService petService;

    @BeforeEach
    void setUp() {
        petService = new PetService(petRepository, tutorRepository, new PetMapper());
    }

    @Nested
    @DisplayName("Cadastro de pets.")
    class CadastroPet {

        @Test
        @DisplayName("PCE - Deve cadastrar pet quando o tutor existir.")
        void deveCadastrarPet_quandoTutorExistir() {
            var tutor = criarTutor();
            var request = PetTestData.criarPetRequest();

            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.of(tutor));
            when(petRepository.save(any(Pet.class))).thenAnswer(i -> {
                Pet pet = i.getArgument(0);
                ReflectionTestUtils.setField(pet, "id", 1L);
                return pet;
            });

            var response = petService.cadastrar(request, 1L);

            assertThat(response).isNotNull();
            assertThat(response.nome()).isEqualTo("Fluffy");
            assertThat(response.especie()).isEqualTo("Gato");
            assertThat(response.raca()).isEqualTo("Persa");
            assertThat(response.idade()).isEqualTo(2);

            verify(tutorRepository).findActiveById(1L);
            verify(petRepository).save(any(Pet.class));
        }

        @Test
        @DisplayName("ESE - Não deve cadastrar pet quando o tutor não existir.")
        void naoDeveCadastrarPet_quandoTutorNaoExistir() {
            var request = PetTestData.criarPetRequest();

            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> petService.cadastrar(request, 1L))
                    .isInstanceOf(TutorNotFoundException.class);

            verify(tutorRepository).findActiveById(1L);
            verify(petRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Listagem de pets.")
    class ListagemPets {

        @Test
        @DisplayName("PCE - Deve listar os pets do tutor quando existirem.")
        void deveListarPets_quandoExistirem() {
            var tutor = criarTutor();
            var pet = PetTestData.criarPet(tutor);

            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.of(tutor));
            when(petRepository.findByTutorIdAndDeletedAtIsNull(1L)).thenReturn(List.of(pet));

            var response = petService.listarPorTutor(1L);

            assertThat(response).hasSize(1);
            assertThat(response.getFirst().id()).isEqualTo(1L);
            assertThat(response.getFirst().nome()).isEqualTo("Fluffy");
        }

        @Test
        @DisplayName("PCE - Deve retornar lista vazia quando não houver pets cadastrados.")
        void deveRetornarListaVazia_quandoNaoExistiremPets() {
            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.of(criarTutor()));
            when(petRepository.findByTutorIdAndDeletedAtIsNull(1L)).thenReturn(List.of());

            var response = petService.listarPorTutor(1L);

            assertThat(response).isEmpty();
        }

        @Test
        @DisplayName("ESE - Não deve listar pets quando o tutor não existir.")
        void naoDeveListarPets_quandoTutorNaoExistir() {
            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> petService.listarPorTutor(1L))
                    .isInstanceOf(TutorNotFoundException.class);

            verify(tutorRepository).findActiveById(1L);
            verify(petRepository, never()).findByTutorIdAndDeletedAtIsNull(any());
        }

        @Test
        @DisplayName("ESE - Não deve listar pets excluídos logicamente.")
        void naoDeveListarPets_quandoExcluidosLogicamente() {
            var tutor = criarTutor();

            when(tutorRepository.findActiveById(1L)).thenReturn(Optional.of(tutor));
            when(petRepository.findByTutorIdAndDeletedAtIsNull(1L)).thenReturn(List.of());

            var response = petService.listarPorTutor(1L);

            assertThat(response).isEmpty();
            verify(petRepository).findByTutorIdAndDeletedAtIsNull(1L);
        }
    }

    @Nested
    @DisplayName("Busca de pet por id.")
    class BuscaPet {

        @Test
        @DisplayName("PCE - Deve buscar pet quando o id e tutor existirem.")
        void deveBuscarPet_quandoIdEtutorExistirem() {
            var tutor = criarTutor();
            var pet = PetTestData.criarPet(tutor);

            when(petRepository.findByIdAndTutorIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(pet));

            var response = petService.buscarPorId(1L, 1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.nome()).isEqualTo("Fluffy");
            assertThat(response.especie()).isEqualTo("Gato");
        }

        @Test
        @DisplayName("ESE - Não deve buscar pet quando o id não existir.")
        void naoDeveBuscarPet_quandoIdNaoExistir() {
            when(petRepository.findByIdAndTutorIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> petService.buscarPorId(1L, 1L))
                    .isInstanceOf(PetNotFoundException.class);
        }

        @Test
        @DisplayName("ESE - Não deve buscar pet excluído logicamente.")
        void naoDeveBuscarPet_quandoExcluidoLogicamente() {
            when(petRepository.findByIdAndTutorIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> petService.buscarPorId(1L, 1L))
                    .isInstanceOf(PetNotFoundException.class)
                    .hasMessageContaining("1");
        }
    }

    @Nested
    @DisplayName("Atualização de pets.")
    class AtualizacaoPet {

        @Test
        @DisplayName("PCE - Deve atualizar pet quando o id e tutor existirem.")
        void deveAtualizarPet_quandoIdETutorExistirem() {
            var tutor = criarTutor();
            var pet = PetTestData.criarPet(tutor);
            var request = new PetRequest(
                    "Fluffy Atualizado",
                    "Gato",
                    "Siamês",
                    3,
                    new BigDecimal("6.50")
            );

            when(petRepository.findByIdAndTutorIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(pet));
            when(petRepository.save(any(Pet.class))).thenAnswer(i -> i.getArgument(0));

            var response = petService.atualizar(1L, 1L, request);

            assertThat(response.nome()).isEqualTo("Fluffy Atualizado");
            assertThat(response.raca()).isEqualTo("Siamês");
            assertThat(response.idade()).isEqualTo(3);
            assertThat(pet.getNome()).isEqualTo("Fluffy Atualizado");

            verify(petRepository).save(pet);
        }

        @Test
        @DisplayName("ESE - Não deve atualizar pet quando o id não existir.")
        void naoDeveAtualizarPet_quandoIdNaoExistir() {
            var request = PetTestData.criarPetRequest();

            when(petRepository.findByIdAndTutorIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> petService.atualizar(1L, 1L, request))
                    .isInstanceOf(PetNotFoundException.class);

            verify(petRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Exclusão de pets.")
    class ExclusaoPet {

        @Test
        @DisplayName("PCE - Deve marcar o pet como excluído quando o id e tutor existirem.")
        void deveExcluirPet_quandoIdETutorExistirem() {
            var tutor = criarTutor();
            var pet = PetTestData.criarPet(tutor);

            when(petRepository.findByIdAndTutorIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(pet));

            petService.deletar(1L, 1L);

            verify(petRepository).softDelete(1L);
            verify(petRepository, never()).save(any());
        }

        @Test
        @DisplayName("ESE - Não deve excluir pet quando o id não existir.")
        void naoDeveExcluirPet_quandoIdNaoExistir() {
            when(petRepository.findByIdAndTutorIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> petService.deletar(1L, 1L))
                    .isInstanceOf(PetNotFoundException.class);

            verify(petRepository, never()).save(any());
        }
    }

    private Tutor criarTutor() {
        var tutor = new Tutor(
                "Wallace",
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
