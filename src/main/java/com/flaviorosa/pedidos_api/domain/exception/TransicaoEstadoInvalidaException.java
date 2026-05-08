package com.flaviorosa.pedidos_api.domain.exception;

public class TransicaoEstadoInvalidaException extends RuntimeException {
    public TransicaoEstadoInvalidaException(String message) {
        super(message);
    }
}
