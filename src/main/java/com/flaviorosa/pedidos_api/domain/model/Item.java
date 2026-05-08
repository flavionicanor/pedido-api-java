package com.flaviorosa.pedidos_api.domain.model;

import java.math.BigDecimal;

// record: classe de dados imutável — gera construtor, getters, equals, hashCode, toString
public record Item (String produtoId, String nome, int quantidade, BigDecimal precoUnitario) {
    // compact constructor — valida os dados na criação
    public Item {
        // compact constructor com validação
        if(quantidade <= 0)
            throw new IllegalArgumentException("Quantidade deve ser positiva");

        if(precoUnitario.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Preco deve ser positivo");

    }

    // método de negócio no record
    public BigDecimal subTotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }


}
