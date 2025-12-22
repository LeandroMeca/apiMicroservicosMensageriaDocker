package io.github.leandro.mscartoes.aplication;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.leandro.mscartoes.aplication.representation.CartaoSaveRequest;
import io.github.leandro.mscartoes.aplication.representation.CartoesPorClienteResponse;
import io.github.leandro.mscartoes.domain.Cartao;
import io.github.leandro.mscartoes.domain.ClienteCartao;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("cartoes")
@RequiredArgsConstructor
public class CartoesResource {

    private final CartaoService cartaoService;
    private final ClienteCartaoService clienteCartaoService;


    @PostMapping
    public ResponseEntity cadastra(@RequestBody CartaoSaveRequest request){
        Cartao cartao = request.toModel();
        cartaoService.save(cartao);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(params = "renda")
    public ResponseEntity<List<Cartao>> getCartoesRendaAte(@RequestParam("renda") Long renda){
        List<Cartao> lista = cartaoService.getCartoesRendaMenorIgual(renda);
        return ResponseEntity.ok(lista);
    }

    @GetMapping(params = "cpf")
    public ResponseEntity<List<CartoesPorClienteResponse>> getCartoesByClient(@RequestParam("cpf") String cpf){
       List<ClienteCartao> lista = clienteCartaoService.listarCartoesPorCpf(cpf);
       List<CartoesPorClienteResponse> resultList = lista.stream()
            .map(CartoesPorClienteResponse::fromModel)
            .collect(Collectors.toList());

         return ResponseEntity.ok(resultList);
    }

}
