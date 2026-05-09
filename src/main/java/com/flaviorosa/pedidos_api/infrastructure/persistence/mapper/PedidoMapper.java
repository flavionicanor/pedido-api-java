package com.flaviorosa.pedidos_api.infrastructure.persistence.mapper;

import com.flaviorosa.pedidos_api.domain.model.Item;
import com.flaviorosa.pedidos_api.domain.model.Pedido;
import com.flaviorosa.pedidos_api.domain.model.StatusPedido;
import com.flaviorosa.pedidos_api.infrastructure.persistence.entity.ItemEntity;
import com.flaviorosa.pedidos_api.infrastructure.persistence.entity.PedidoEntity;
import com.flaviorosa.pedidos_api.infrastructure.persistence.entity.StatusPedidoEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PedidoMapper {

    // CRIAÇÃO — sem ID, o @GeneratedValue do banco gera
    public PedidoEntity toNewEntity(Pedido pedido) {
        PedidoEntity entity = new PedidoEntity();
        // sem setId — banco gera via @GeneratedValue
        entity.setClienteId(pedido.getClienteId());
        entity.setStatus(toEnum(pedido.getStatus()));
        entity.setCriadoEm(pedido.getCriadoEm());
        preencherCamposStatus(entity, pedido.getStatus());

        pedido.getItens().forEach(item -> {
            ItemEntity itemEntity = toItemEntity(item);
            entity.adicionarItem(itemEntity);
        });

        return entity;
    }

    // ATUALIZAÇÃO — com ID, o JPA sabe qual registro atualizar
    public PedidoEntity toEntity(Pedido pedido) {
        PedidoEntity entity = new PedidoEntity();
        entity.setId(pedido.getId());           // ← seta o ID para o merge funcionar
        entity.setClienteId(pedido.getClienteId());
        entity.setStatus(toEnum(pedido.getStatus()));
        entity.setCriadoEm(pedido.getCriadoEm());
        preencherCamposStatus(entity, pedido.getStatus());

        pedido.getItens().forEach(item -> {
            ItemEntity itemEntity = toItemEntity(item);
            entity.adicionarItem(itemEntity);
        });

        return entity;
    }

    // Entidade → Domínio
    public Pedido toDomain(PedidoEntity entity) {
        List<Item> itens = entity.getItens().stream()
                .map(this::toItemDomain)
                .toList();

        return new Pedido(
                entity.getId(),
                entity.getClienteId(),
                itens,
                toStatusDomain(entity),   // agora usa os campos reais do banco
                entity.getCriadoEm()
        );
    }

    // preenche os campos extras de acordo com o status
    private void preencherCamposStatus(PedidoEntity entity, StatusPedido status) {
        switch (status) {
            case StatusPedido.Processando p -> entity.setResponsavel(p.responsavel());
            case StatusPedido.Concluido c   -> entity.setConcluidoEm(c.concluidoEm());
            case StatusPedido.Cancelado c   -> entity.setMotivoCancelamento(c.motivo());
            default -> {}
        }
    }

    // reconstrói o status usando os campos reais salvos no banco
    private StatusPedido toStatusDomain(PedidoEntity entity) {
        return switch (entity.getStatus()) {
            case AGUARDANDO  -> new StatusPedido.Aguardando();
            case PROCESSANDO -> new StatusPedido.Processando(
                    entity.getResponsavel() != null ? entity.getResponsavel() : "sistema");
            case CONCLUIDO   -> new StatusPedido.Concluido(
                    entity.getConcluidoEm() != null ? entity.getConcluidoEm() : entity.getCriadoEm());
            case CANCELADO   -> new StatusPedido.Cancelado(
                    entity.getMotivoCancelamento() != null ? entity.getMotivoCancelamento() : "");
        };
    }

    private ItemEntity toItemEntity(Item item) {
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setProdutoId(item.produtoId());
        itemEntity.setNome(item.nome());
        itemEntity.setQuantidade(item.quantidade());
        itemEntity.setPrecoUnitario(item.precoUnitario());
        return itemEntity;
    }

    private Item toItemDomain(ItemEntity entity) {
        return new Item(
                entity.getProdutoId(),
                entity.getNome(),
                entity.getQuantidade(),
                entity.getPrecoUnitario()
        );
    }

    private StatusPedidoEnum toEnum(StatusPedido status) {
        return switch (status) {
            case StatusPedido.Aguardando a  -> StatusPedidoEnum.AGUARDANDO;
            case StatusPedido.Processando p -> StatusPedidoEnum.PROCESSANDO;
            case StatusPedido.Concluido c   -> StatusPedidoEnum.CONCLUIDO;
            case StatusPedido.Cancelado c   -> StatusPedidoEnum.CANCELADO;
        };
    }
}
