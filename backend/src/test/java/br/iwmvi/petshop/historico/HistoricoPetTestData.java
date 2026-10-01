package br.iwmvi.petshop.historico;

import br.iwmvi.petshop.funcionario.model.Funcionario;
import br.iwmvi.petshop.historico.dto.request.HistoricoPetRequest;
import br.iwmvi.petshop.historico.model.HistoricoPet;
import br.iwmvi.petshop.historico.model.TipoEvento;
import br.iwmvi.petshop.pet.model.Pet;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

public class HistoricoPetTestData {

    public static final LocalDateTime DATA_EVENTO = LocalDateTime.of(2026, 1, 10, 14, 30);

    public static HistoricoPetRequest criarHistoricoRequest() {
        return new HistoricoPetRequest(
                TipoEvento.VACINACAO,
                "Vacina V10 - primeira dose",
                DATA_EVENTO,
                1L
        );
    }

    public static HistoricoPet criarHistorico(Pet pet, Funcionario funcionario) {
        var historico = new HistoricoPet(
                pet,
                funcionario,
                TipoEvento.VACINACAO,
                "Vacina V10 - primeira dose",
                DATA_EVENTO
        );

        ReflectionTestUtils.setField(historico, "id", 1L);

        return historico;
    }
}
