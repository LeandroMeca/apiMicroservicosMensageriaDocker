package io.github.leandro.msavaliadorcredito.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import feign.FeignException;
import io.github.leandro.msavaliadorcredito.application.ex.ClienteException;
import io.github.leandro.msavaliadorcredito.application.ex.ErroComunicacaoMicroservicesException;
import io.github.leandro.msavaliadorcredito.application.ex.ErroSolicitacaoCartaoException;
import io.github.leandro.msavaliadorcredito.domain.model.Cartao;
import io.github.leandro.msavaliadorcredito.domain.model.CartaoAprovado;
import io.github.leandro.msavaliadorcredito.domain.model.CartaoCliente;
import io.github.leandro.msavaliadorcredito.domain.model.DadosCliente;
import io.github.leandro.msavaliadorcredito.domain.model.DadosSolicitacaoEmissaoCartao;
import io.github.leandro.msavaliadorcredito.domain.model.ProtocoloSolicitacaoCartao;
import io.github.leandro.msavaliadorcredito.domain.model.RetornoAvaliacaoCliente;
import io.github.leandro.msavaliadorcredito.domain.model.SituacaoCliente;
import io.github.leandro.msavaliadorcredito.infra.clients.CartoesResourceClient;
import io.github.leandro.msavaliadorcredito.infra.clients.ClienteResourceClient;
import io.github.leandro.msavaliadorcredito.infra.mqueue.SolicitacaoEmissaoCartaoPublisher;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvaliadorCreditoService {

    private final ClienteResourceClient clienteResourceClient;
    private final CartoesResourceClient cartoesResourceClient;
    private final SolicitacaoEmissaoCartaoPublisher emissaoCartaoPublisher;

    public SituacaoCliente obterSituacaoCliente(String cpf) throws ClienteException, ErroComunicacaoMicroservicesException {
       
       try{
       ResponseEntity<DadosCliente> clienteResponse = clienteResourceClient.dadosCliente(cpf);
       ResponseEntity<List<CartaoCliente>> cartoesResponse = cartoesResourceClient.getCartoesByClient(cpf);

       return SituacaoCliente.builder().cliente(clienteResponse.getBody()) .cartoes(cartoesResponse.getBody()).build();
       }catch(FeignException.FeignClientException e){
        int status = e.status();
        if(HttpStatus.NOT_FOUND.value() == status){
            throw new ClienteException();
        }
        throw new ErroComunicacaoMicroservicesException(e.getMessage(), status);
       }

    } 

    public RetornoAvaliacaoCliente realizarAvaliacao(String cpf, Long renda) throws ClienteException, ErroComunicacaoMicroservicesException{
        try {
            ResponseEntity<DadosCliente> dadosClienteResponse = clienteResourceClient.dadosCliente(cpf);
            ResponseEntity<List<Cartao>> cartoesResponse = cartoesResourceClient.getCartoesRendaAteh(renda);

            List<Cartao> cartoes = cartoesResponse.getBody();

            var listaCartoesAprovados = cartoes.stream().map(cartao -> {

                DadosCliente dadosCliente = dadosClienteResponse.getBody();

                BigDecimal limiteBasico = cartao.getLimiteBasico();
                BigDecimal idadeBD = BigDecimal.valueOf(dadosCliente.getIdade());

                var fator = idadeBD.divide(BigDecimal.valueOf(10));
                BigDecimal limiteAprovado = fator.multiply(limiteBasico);

                CartaoAprovado aprovado = new CartaoAprovado();
                aprovado.setCartao(cartao.getNome());
                aprovado.setBandeira(cartao.getBandeira());
                aprovado.setLimiteAprovado(limiteAprovado);

                return aprovado;
            }).collect(Collectors.toList());

            return new RetornoAvaliacaoCliente(listaCartoesAprovados);

        } catch (FeignException.FeignClientException e) {
            // TODO: handle exception
            int status = e.status();
        if(HttpStatus.NOT_FOUND.value() == status){
            throw new ClienteException();
        }
        throw new ErroComunicacaoMicroservicesException(e.getMessage(), status);
        }
    
    }


    public ProtocoloSolicitacaoCartao solicitacaoEmissaoCartao(DadosSolicitacaoEmissaoCartao dados) {
        try {
            emissaoCartaoPublisher.solicitarCartao(dados);
            var protocolo = UUID.randomUUID().toString();
            return new ProtocoloSolicitacaoCartao(protocolo);
        } catch (Exception e) {
            throw new ErroSolicitacaoCartaoException(e.getMessage());
        }

    }








}
