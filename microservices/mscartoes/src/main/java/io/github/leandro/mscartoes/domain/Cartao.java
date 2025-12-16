package io.github.leandro.mscartoes.domain;

import java.math.BigDecimal;

import javax.persistence.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Cartao {

    private Long id;
    private String nome;
    private String bandeira;
    private Integer renda;
    private BigDecimal limiteBasico;
}
