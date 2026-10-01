package br.iwmvi.petshop.funcionario.model;

import br.iwmvi.petshop.common.entity.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "funcionarios")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Funcionario extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Cargo cargo;

    @Column(nullable = false, length = 11)
    private String telefone;

    public Funcionario(String nome, String cpf, Cargo cargo, String telefone) {
        this.nome = nome;
        this.cpf = cpf;
        this.cargo = cargo;
        this.telefone = telefone;
    }

    public void atualizar(String nome, String cpf, Cargo cargo, String telefone) {
        this.nome = nome;
        this.cpf = cpf;
        this.cargo = cargo;
        this.telefone = telefone;
    }
}
