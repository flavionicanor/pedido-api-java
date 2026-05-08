package com.flaviorosa.pedidos_api.infrastructure.web.dto;

import com.flaviorosa.pedidos_api.domain.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        String id,
        String clienteId,
        BigDecimal total,
        String status,
        LocalDateTime criadoEm,
        List<ItemResponse> itens
) {
    // Factory method — converte Pedido (domínio) → PedidoResponse (DTO)
    public static PedidoResponse from(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.total(),
                pedido.getStatus().descricao(),
                pedido.getCriadoEm(),
                pedido.getItens().stream().map(ItemResponse::from).toList()
        );
    }
}
