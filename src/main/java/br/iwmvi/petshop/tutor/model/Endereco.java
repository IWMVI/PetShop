package br.iwmvi.petshop.tutor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Endereco {

    @Column(nullable = false, length = 8)
    private String cep;

    @Column(nullable = false, length = 150)
    private String logradouro;

    @Column(length = 6)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(nullable = false, length = 150)
    private String bairro;

    @Column(nullable = false, length = 150)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String estado;
}
