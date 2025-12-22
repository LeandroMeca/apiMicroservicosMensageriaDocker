package io.github.leandro.mscartoes.infra.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.leandro.mscartoes.domain.Cartao;

public interface CartaoRepository extends JpaRepository<Cartao, Long>{

    List<Cartao> findByRendaLessThanEqual(BigDecimal renda);

}
