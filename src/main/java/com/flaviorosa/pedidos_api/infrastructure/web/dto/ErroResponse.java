package com.flaviorosa.pedidos_api.infrastructure.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErroResponse(
        int status ,
        String erro,
        String mensagem,
        LocalDateTime timestamp,
        List<String> detalhes
) {
    // para erros simples sem lista de detalhes
    public ErroResponse(int status, String erro, String mensagem){
        this(status, erro, mensagem, LocalDateTime.now(), List.of());
    }
}
