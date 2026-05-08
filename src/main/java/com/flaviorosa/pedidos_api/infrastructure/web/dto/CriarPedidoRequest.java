package com.flaviorosa.pedidos_api.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CriarPedidoRequest(
        @NotBlank(message = "Cliente é obrigatório")
        String clienteId,
        @NotEmpty(message = "Pedido deve ter pelo menos um item")
        @Valid
        List<ItemRequest> itens
) {
}
