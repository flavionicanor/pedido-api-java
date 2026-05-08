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

    // Domínio → Entidade (para salvar no banco)
    public PedidoEntity toEntity(Pedido pedido) {
        PedidoEntity entity = new PedidoEntity();
        entity.setClienteId(pedido.getClienteId());
        entity.setStatus(toEnum(pedido.getStatus()));
        entity.setCriadoEm(pedido.getCriadoEm());

        pedido.getItens().forEach(item ->{
            ItemEntity itemEntity = toItemEntity(item);
            entity.adicionarItem(itemEntity);
        });

        return entity;
    }

    // Entidade → Domínio (para usar na lógica de negócio)
    public Pedido toDomain(PedidoEntity entity) {
        List<Item> itens = entity.getItens().stream()
                .map(this::toItemDomain)
                .toList();

        return new Pedido(
                entity.getId(),
                entity.getClienteId(),
                itens,
                toStatusDomain(entity.getStatus(), entity),
                entity.getCriadoEm()
        );
    }

    private ItemEntity toItemEntity(Item item){
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setProdutoId(item.produtoId());
        itemEntity.setNome(item.nome());
        itemEntity.setQuantidade(item.quantidade());
        itemEntity.setPrecoUnitario(item.precoUnitario());
        return itemEntity;
    }

    private Item toItemDomain(ItemEntity entity){
        return new Item(
                entity.getProdutoId(),
                entity.getNome(),
                entity.getQuantidade(),
                entity.getPrecoUnitario()
        );
    }

    private StatusPedidoEnum toEnum(StatusPedido status){
        return switch (status){
            case StatusPedido.Aguardando a -> StatusPedidoEnum.AGUARDANDO;
            case StatusPedido.Processando p -> StatusPedidoEnum.PROCESSANDO;
            case StatusPedido.Concluido c -> StatusPedidoEnum.CONCLUIDO;
            case StatusPedido.Cancelado c -> StatusPedidoEnum.CANCELADO;
        };
    }

    private StatusPedido toStatusDomain(StatusPedidoEnum status, PedidoEntity entity){
        return switch (status){
            case AGUARDANDO -> new StatusPedido.Aguardando();
            case PROCESSANDO -> new StatusPedido.Processando("sistema");
            case CONCLUIDO -> new StatusPedido.Concluido(LocalDateTime.now());
            case CANCELADO -> new StatusPedido.Cancelado("restaurado do banco");
        };
    }

}
