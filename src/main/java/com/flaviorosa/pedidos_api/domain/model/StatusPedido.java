package com.flaviorosa.pedidos_api.domain.model;

import java.time.LocalDateTime;

// sealed = só quem está em "permits" pode implementar esta interface
public sealed interface StatusPedido
        // permits = lista fechada dos únicos implementadores permitidos
        permits StatusPedido.Aguardando, StatusPedido.Processando,
        StatusPedido.Concluido, StatusPedido.Cancelado {

    // record sem campos — o estado "Aguardando" não precisa de dados extras
    record Aguardando() implements StatusPedido {}

    // record com um campo — guarda QUEM está processando
    record Processando(String responsavel) implements StatusPedido {}

    // record com um campo — guarda QUANDO foi concluído
    record Concluido(LocalDateTime concluidoEm) implements StatusPedido {}

    // record com um campo — guarda POR QUE foi cancelado
    record Cancelado(String motivo) implements StatusPedido {}

    // default = método com implementação dentro da interface
    // "this" aqui é a instância concreta (Aguardando, Processando, etc.)
    default String descricao() {
        // switch com pattern matching — verifica o tipo E extrai a variável
        return switch (this) {
            // "a" é do tipo Aguardando — mas não usamos nenhum campo
            case Aguardando a  -> "Aguardando processamento";

            // "p" é do tipo Processando — acessamos p.responsavel() direto
            case Processando p -> "Em processamento por " + p.responsavel();

            // "c" é do tipo Concluido — acessamos c.concluidoEm() direto
            case Concluido c   -> "Concluído em " + c.concluidoEm();

            // "c" é do tipo Cancelado — acessamos c.motivo() direto
            case Cancelado c   -> "Cancelado: " + c.motivo();

            // sem "default" — o compilador sabe que os 4 casos cobrem tudo
        };
    }
}