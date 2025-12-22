package io.github.leandro.mscartoes.aplication;

import java.util.List;

import org.springframework.stereotype.Service;

import io.github.leandro.mscartoes.domain.ClienteCartao;
import io.github.leandro.mscartoes.infra.repository.ClienteCartaoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteCartaoService {

    private final ClienteCartaoRepository clienteCartaoRepository;

    public List<ClienteCartao> listarCartoesPorCpf(String cpf){
        return clienteCartaoRepository.findByCpf(cpf);
    }




}
