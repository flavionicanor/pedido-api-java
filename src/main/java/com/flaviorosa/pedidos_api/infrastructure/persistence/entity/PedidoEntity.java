package com.flaviorosa.pedidos_api.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "pedidos",
        indexes = {
                // índices aceleram buscas — sem eles o banco faz full scan
                @Index(name = "idx_pedidos_cliente_id", columnList = "cliente_id"),
                @Index(name = "idx_pedidos_status",     columnList = "status"),
                @Index(name = "idx_pedidos_criado_em",  columnList = "criado_em")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "cliente_id", nullable = false, length = 36)
    private String clienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedidoEnum status;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    // campos de status — preenchidos conforme a transição
    @Column(length = 100)
    private String responsavel;          // preenchido quando PROCESSANDO

    @Column(name = "concluido_em")
    private LocalDateTime concluidoEm;   // preenchido quando CONCLUIDO

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;   // preenchido quando CANCELADO

    @OneToMany(
            mappedBy = "pedido",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ItemEntity> itens = new ArrayList<>();

    public void adicionarItem(ItemEntity item) {
        item.setPedido(this);
        this.itens.add(item);
    }
}
