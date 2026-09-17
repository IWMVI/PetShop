package br.iwmvi.petshop.pet.model;

import br.iwmvi.petshop.tutor.model.Tutor;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "pets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 50)
    private String especie;

    @Column(precision = 5, scale = 2)
    private BigDecimal peso;

    @Column(length = 100)
    private String raca;

    private Integer idade;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    public Pet(String nome, String especie, BigDecimal peso, String raca, Integer idade, Tutor tutor) {
        this.nome = nome;
        this.especie = especie;
        this.peso = peso;
        this.raca = raca;
        this.idade = idade;
        this.tutor = tutor;
    }

    public void atualizar(String nome, String especie, BigDecimal peso, String raca, Integer idade) {
        this.nome = nome;
        this.especie = especie;
        this.peso = peso;
        this.raca = raca;
        this.idade = idade;
    }

    public void deletar() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restaurar() {
        this.deletedAt = null;
    }

}
