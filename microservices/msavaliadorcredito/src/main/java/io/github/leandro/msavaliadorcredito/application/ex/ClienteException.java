package io.github.leandro.msavaliadorcredito.application.ex;

public class ClienteException extends Exception{


    public ClienteException(){
        super("Dados do cliente não foram encontrados");
    }

    
}
