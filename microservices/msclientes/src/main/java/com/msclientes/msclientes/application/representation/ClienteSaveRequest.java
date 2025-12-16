package com.msclientes.msclientes.application.representation;

import com.msclientes.msclientes.domain.Cliente;

import lombok.Data;

@Data
public class ClienteSaveRequest {

    private String nome;
    private String cpf;
    private Integer idade;

    public Cliente toModel() {
        return new Cliente(cpf, nome, idade);
    }

    

}
