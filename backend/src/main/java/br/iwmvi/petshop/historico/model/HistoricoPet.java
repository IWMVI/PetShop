package br.iwmvi.petshop.historico.model;

import br.iwmvi.petshop.funcionario.model.Funcionario;
import br.iwmvi.petshop.pet.model.Pet;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Evento da vida do pet. Registro imutável: não há setters nem métodos de atualização,
 * e o {@link Immutable} faz o Hibernate ignorar qualquer UPDATE.
 */
@Entity
@Getter
@Immutable
@Table(name = "historico_pets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoricoPet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEvento tipoEvento;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime dataEvento;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public HistoricoPet(Pet pet, Funcionario funcionario, TipoEvento tipoEvento,
                        String descricao, LocalDateTime dataEvento) {
        this.pet = pet;
        this.funcionario = funcionario;
        this.tipoEvento = tipoEvento;
        this.descricao = descricao;
        this.dataEvento = dataEvento;
    }
}
