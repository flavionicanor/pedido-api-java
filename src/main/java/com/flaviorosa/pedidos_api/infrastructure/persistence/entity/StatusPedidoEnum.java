package com.flaviorosa.pedidos_api.infrastructure.persistence.entity;

// Enum salvo como String no banco ("AGUARDANDO", "PROCESSANDO"...)
// É a representação do status no banco — separado do domínio
public enum StatusPedidoEnum {
    AGUARDANDO,
    PROCESSANDO,
    CONCLUIDO,
    CANCELADO
}
