package com.flaviorosa.pedidos_api.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemRequest(
        @NotBlank(message = "Produto é obrigatório")
        String produtoId,

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @Min(value = 1, message = "Quantidade mínima é 1")
        int quantidade,

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço deve ser positivo")
        BigDecimal precoUnitario
) {
}
