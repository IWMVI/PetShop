package br.iwmvi.petshop.agendamento.model;

import br.iwmvi.petshop.pet.model.Pet;
import br.iwmvi.petshop.servico.model.Servico;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Table(name = "agendamentos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AgendamentoStatus status;

    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "agendamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<AgendamentoServico> agendamentoServicos = new ArrayList<>();

    public Agendamento(Pet pet, LocalDateTime dataHora, String observacoes,
                       AgendamentoStatus status, BigDecimal valorTotal) {
        this.pet = pet;
        this.dataHora = dataHora;
        this.observacoes = observacoes;
        this.status = status;
        this.valorTotal = valorTotal;
    }

    public void atualizar(LocalDateTime dataHora, String observacoes, BigDecimal valorTotal) {
        this.dataHora = dataHora;
        this.observacoes = observacoes;
        this.valorTotal = valorTotal;
        this.status = AgendamentoStatus.AGENDADO;
        this.deletedAt = null;
    }

    public void definirServicos(List<Servico> servicos) {
        var precosExistentes = agendamentoServicos.stream()
                .collect(Collectors.toMap(
                        as -> as.getServico().getId(),
                        AgendamentoServico::getPrecoCobrado
                ));

        agendamentoServicos.clear();
        for (Servico servico : servicos) {
            var precoCobrado = precosExistentes.getOrDefault(servico.getId(), servico.getPreco());
            agendamentoServicos.add(new AgendamentoServico(this, servico, precoCobrado));
        }
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public void cancelar() {
        this.status = AgendamentoStatus.CANCELADO;
        this.deletedAt = LocalDateTime.now();
    }
}
