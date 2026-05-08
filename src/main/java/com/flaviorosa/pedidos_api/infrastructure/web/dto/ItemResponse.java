package com.flaviorosa.pedidos_api.infrastructure.web.dto;

import com.flaviorosa.pedidos_api.domain.model.Item;

import java.math.BigDecimal;

public record ItemResponse(
        String produtoId,
        String nome,
        int quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
    public static ItemResponse from(Item item) {
        return new ItemResponse(
                item.produtoId(),
                item.nome(),
                item.quantidade(),
                item.precoUnitario(),
                item.subTotal()
        );
    }
}
