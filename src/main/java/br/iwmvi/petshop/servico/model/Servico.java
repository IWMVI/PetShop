package br.iwmvi.petshop.servico.model;

import br.iwmvi.petshop.common.entity.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "servicos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Servico extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "duracao_minutos")
    private Integer tempoEstimadoMinutos;

    public Servico(String nome, String descricao, BigDecimal preco, Integer tempoEstimadoMinutos) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.tempoEstimadoMinutos = tempoEstimadoMinutos;
    }

    public void atualizar(String nome, String descricao, BigDecimal preco, Integer tempoEstimadoMinutos) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.tempoEstimadoMinutos = tempoEstimadoMinutos;
    }
}
