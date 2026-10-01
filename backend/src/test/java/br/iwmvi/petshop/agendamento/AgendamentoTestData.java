package br.iwmvi.petshop.agendamento;

import br.iwmvi.petshop.tutor.CpfTestData;
import br.iwmvi.petshop.agendamento.dto.request.AgendamentoRequest;
import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.agendamento.model.AgendamentoStatus;
import br.iwmvi.petshop.endereco.model.Endereco;
import br.iwmvi.petshop.pet.model.Pet;
import br.iwmvi.petshop.servico.model.Servico;
import br.iwmvi.petshop.tutor.model.Tutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AgendamentoTestData {

    public static AgendamentoRequest criarAgendamentoRequest(LocalDateTime dataHora) {
        return new AgendamentoRequest(
                dataHora,
                "Levar guia e carteira de vacinação",
                List.of(10L, 11L)
        );
    }

    public static Pet criarPet() {
        var tutor = new Tutor(
                "Wallace",
                CpfTestData.VALIDO,
                "wallace@test.com",
                "11999999999",
                new Endereco("01001010", "Praça da Sé", "1", null, "Sé", "São Paulo", "SP")
        );
        ReflectionTestUtils.setField(tutor, "id", 1L);

        var pet = new Pet("Rex", "Cachorro", new BigDecimal("12.50"), "SRD", 4, tutor);
        ReflectionTestUtils.setField(pet, "id", 2L);

        return pet;
    }

    public static Servico criarServico(Long id, String nome, String preco) {
        var servico = new Servico(nome, nome + " descrição", new BigDecimal(preco), 60);
        ReflectionTestUtils.setField(servico, "id", id);
        return servico;
    }

    public static Agendamento criarAgendamento(Pet pet, LocalDateTime dataHora, List<Servico> servicos) {
        var agendamento = new Agendamento(
                pet,
                dataHora,
                "Observações iniciais",
                AgendamentoStatus.AGENDADO,
                servicos.stream().map(Servico::getPreco).reduce(BigDecimal.ZERO, BigDecimal::add)
        );
        agendamento.definirServicos(servicos);
        ReflectionTestUtils.setField(agendamento, "id", 50L);
        return agendamento;
    }
}
