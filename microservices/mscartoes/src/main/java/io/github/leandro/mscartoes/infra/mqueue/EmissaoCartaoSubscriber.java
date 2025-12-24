package io.github.leandro.mscartoes.infra.mqueue;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.leandro.mscartoes.domain.Cartao;
import io.github.leandro.mscartoes.domain.ClienteCartao;
import io.github.leandro.mscartoes.domain.DadosSolicitacaoEmissaoCartao;
import io.github.leandro.mscartoes.infra.repository.CartaoRepository;
import io.github.leandro.mscartoes.infra.repository.ClienteCartaoRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmissaoCartaoSubscriber {

    private final CartaoRepository cartaoRepository;
    private final ClienteCartaoRepository clienteCartaoRepository;

    @RabbitListener(queues = "${mq.queues.emissao-cartoes}")
    public void receberSolicitacaoEmissao(@Payload String payLoad) {
       
        try{
            var mapper = new ObjectMapper();
            
            DadosSolicitacaoEmissaoCartao dados = mapper.readValue(payLoad, DadosSolicitacaoEmissaoCartao.class);
            Cartao cartao = cartaoRepository.findById(dados.getIdCartao()).orElseThrow();
            ClienteCartao clienteCartao = new ClienteCartao();
            clienteCartao.setCartao(cartao);
            clienteCartao.setCpf(dados.getCpf());
            clienteCartao.setLimite(dados.getLimiteLiberado());

            clienteCartaoRepository.save(clienteCartao);

        }catch(Exception e){
            e.printStackTrace();
        }
    
    }



}
