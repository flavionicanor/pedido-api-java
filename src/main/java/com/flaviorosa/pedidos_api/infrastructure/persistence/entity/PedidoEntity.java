package com.flaviorosa.pedidos_api.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "cliente_id", nullable = false, length = 36)
    private String clienteId;

    @Enumerated(EnumType.STRING) // salva "AGUARDANDO" no banco, não "0"
    @Column(nullable = false, length = 20)
    private StatusPedidoEnum status;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    // CascadeType.ALL — operações em Pedido se propagam para os itens
    // orphanRemoval — se remover item da lista, deleta do banco
    // FetchType.LAZY = não carrega itens até você acessar
    @OneToMany(mappedBy = "pedido",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ItemEntity> itens = new ArrayList<>();

    // método auxiliar para manter o relacionamento bidirecional consistente
    public void adicionarItem(ItemEntity item){
        item.setPedido(this);
        this.itens.add(item);
    }
}
