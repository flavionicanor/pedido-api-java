package com.flaviorosa.pedidos_api.domain.exception;

public class PedidoNaoEncontradoException extends RuntimeException {
    public PedidoNaoEncontradoException(String id) {
        super("Pedido não encontrado: " + id);
    }
}
