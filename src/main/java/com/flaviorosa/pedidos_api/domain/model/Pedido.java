package com.flaviorosa.pedidos_api.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

// Pedido.java — usando Generics, Streams e encapsulamento correto
public class Pedido {

    private final String id;
    private final String clienteId;
    private final List<Item> itens;
    private StatusPedido status;
    private final LocalDateTime criadoEm;

    // construtor para pedido NOVO
    public Pedido(String clienteId, List<Item> itens){
        if(itens == null || itens.isEmpty())
            throw new IllegalArgumentException("Pedido deve ter pelo menos um item");

        this.id = UUID.randomUUID().toString();
        this.clienteId = clienteId;

        // cópia defensiva — imutabilidade externa
        this.itens = List.copyOf(itens);

        this.status = new StatusPedido.Aguardando();
        this.criadoEm = LocalDateTime.now();

    }

    // construtor para pedido RECONSTITUÍDO do banco (id e data já existem)
    public Pedido(String id, String clienteId, List<Item> itens, StatusPedido status, LocalDateTime criadoEm){
        this.id = id;
        this.clienteId = clienteId;
        this.itens = List.copyOf(itens);
        this.status = status;
        this.criadoEm = criadoEm;
    }

    // calcula total com stream — soma os subtotais de cada item
    public BigDecimal total() {
        return itens.stream()
                .map(Item::subTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // busca item por produto — retorna Optional para forçar tratamento de ausência
    public Optional<Item> buscarItem(String produtoId){
        return itens.stream()
                .filter(i -> i.produtoId().equals(produtoId))
                .findFirst();
    }

    public Map<String, BigDecimal> subTotalPorProduto(){
        return itens.stream()
                .collect(Collectors.toMap(
                        Item::nome,
                        Item::subTotal
                ));
    }

    // transições de estado — só avança se o estado atual permitir
    public void processar(String responsavel){
        switch(status) {
            case StatusPedido.Aguardando a ->
                this.status = new StatusPedido.Processando(responsavel);
            case StatusPedido.Processando p ->
                throw new IllegalStateException("Pedido já está em processamento por " + p.responsavel());
            default ->
                throw new IllegalStateException("Pedido não pode ser processado: " + status.descricao());    
        }

    }

    public void concluir(){
        if(!(status instanceof StatusPedido.Processando)) {
            throw new IllegalStateException("Só pode concluir pedidos em processamento. Status atual: " + status.descricao());
        }
        this.status = new StatusPedido.Concluido(LocalDateTime.now());
    }

    public void cancelar(String motivo){
        if(status instanceof StatusPedido.Concluido){
            throw new IllegalStateException("Não pode cancelar pedido ja concluido");
        }
        this.status = new StatusPedido.Cancelado(motivo);
    }

    // Getters sem setters — imutabilidade controlada
    public String getId(){
        return id;
    }

    public String getClienteId(){
        return clienteId;
    }

    public List<Item> getItens(){
        return itens;
    }

    public StatusPedido getStatus(){
        return status;
    }

    public LocalDateTime getCriadoEm(){ return criadoEm;}

}
