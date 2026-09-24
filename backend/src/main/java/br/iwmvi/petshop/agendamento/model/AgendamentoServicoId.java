package br.iwmvi.petshop.agendamento.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AgendamentoServicoId implements Serializable {

    @Column(name = "agendamento_id")
    private Long agendamentoId;

    @Column(name = "servico_id")
    private Long servicoId;
}
