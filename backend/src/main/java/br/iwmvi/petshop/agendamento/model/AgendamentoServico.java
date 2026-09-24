package br.iwmvi.petshop.agendamento.model;

import br.iwmvi.petshop.servico.model.Servico;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "agendamento_servicos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgendamentoServico {

    @EmbeddedId
    private AgendamentoServicoId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("agendamentoId")
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("servicoId")
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Column(name = "preco_cobrado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoCobrado;

    public AgendamentoServico(Agendamento agendamento, Servico servico, BigDecimal precoCobrado) {
        this.id = new AgendamentoServicoId();
        this.agendamento = agendamento;
        this.servico = servico;
        this.precoCobrado = precoCobrado;
    }
}
