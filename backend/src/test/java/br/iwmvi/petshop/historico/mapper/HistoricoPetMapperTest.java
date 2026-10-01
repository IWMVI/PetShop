package br.iwmvi.petshop.historico.mapper;

import br.iwmvi.petshop.funcionario.FuncionarioTestData;
import br.iwmvi.petshop.historico.HistoricoPetTestData;
import br.iwmvi.petshop.pet.PetTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HistoricoPetMapperTest {

    private final HistoricoPetMapper mapper = new HistoricoPetMapper();

    @Test
    @DisplayName("PCE - Deve converter request em entidade vinculada ao pet e ao funcionário.")
    void deveConverterRequestEmEntidade() {
        var pet = PetTestData.criarPet(null);
        var funcionario = FuncionarioTestData.criarFuncionario();
        var request = HistoricoPetTestData.criarHistoricoRequest();

        var historico = mapper.toEntity(request, pet, funcionario);

        assertThat(historico.getPet()).isSameAs(pet);
        assertThat(historico.getFuncionario()).isSameAs(funcionario);
        assertThat(historico.getTipoEvento()).isEqualTo(request.tipoEvento());
        assertThat(historico.getDescricao()).isEqualTo(request.descricao());
        assertThat(historico.getDataEvento()).isEqualTo(request.dataEvento());
    }

    @Test
    @DisplayName("PCE - Deve converter entidade em response com os dados do funcionário.")
    void deveConverterEntidadeEmResponse() {
        var historico = HistoricoPetTestData.criarHistorico(
                PetTestData.criarPet(null), FuncionarioTestData.criarFuncionario());

        var response = mapper.toResponse(historico);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.petId()).isEqualTo(1L);
        assertThat(response.funcionarioId()).isEqualTo(1L);
        assertThat(response.funcionarioNome()).isEqualTo("Dra. Ana");
    }

    @Test
    @DisplayName("PCE - Deve converter entidade sem funcionário.")
    void deveConverterEntidadeSemFuncionario() {
        var historico = HistoricoPetTestData.criarHistorico(PetTestData.criarPet(null), null);

        var response = mapper.toResponse(historico);

        assertThat(response.funcionarioId()).isNull();
        assertThat(response.funcionarioNome()).isNull();
    }
}
