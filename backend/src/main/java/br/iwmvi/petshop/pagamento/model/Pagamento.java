package br.iwmvi.petshop.pagamento.model;

import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.common.entity.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "pagamentos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pagamento extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento", nullable = false, length = 50)
    private MetodoPagamento metodoPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatusPagamento status;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;

    public Pagamento(Agendamento agendamento, BigDecimal valor, MetodoPagamento metodoPagamento) {
        this.agendamento = agendamento;
        this.valor = valor;
        this.metodoPagamento = metodoPagamento;
        this.status = StatusPagamento.PENDENTE;
    }

    public void atualizarStatus(StatusPagamento status, LocalDateTime dataPagamento) {
        this.status = status;
        this.dataPagamento = dataPagamento;
    }

    public void cancelar() {
        this.status = StatusPagamento.CANCELADO;
        delete();
    }
}
