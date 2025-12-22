package io.github.leandro.mscartoes.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.leandro.mscartoes.domain.ClienteCartao;
import java.util.List;



public interface ClienteCartaoRepository extends JpaRepository<ClienteCartao, Long> {
    
    List<ClienteCartao> findByCpf(String cpf);

}
