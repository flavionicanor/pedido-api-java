package com.flaviorosa.pedidos_api.infrastructure.persistence.repository;

import com.flaviorosa.pedidos_api.infrastructure.persistence.entity.PedidoEntity;
import com.flaviorosa.pedidos_api.infrastructure.persistence.entity.StatusPedidoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity,String> {

    // @EntityGraph resolve o problema N+1:
    // ao invés de 1 query por pedido para buscar itens,
    // busca tudo em uma query só com JOIN
    @EntityGraph(attributePaths = {"itens"})
    Optional<PedidoEntity> findWithItensById(String id);

    // Spring Data deriva a query pelo nome do método
    Page<PedidoEntity> findByClienteId(String clienteId, Pageable pageable);

    // filtro opcional: se clienteId for null, retorna todos
    @Query("""
            SELECT p FROM PedidoEntity p
            WHERE (:clienteId IS NULL OR p.clienteId = :clienteId)
            AND   (:status IS NULL OR p.status = :status)
            """)
    Page<PedidoEntity> findByFiltros(
            @Param("clienteId") String clienteId,
            @Param("status") StatusPedidoEnum status,
            Pageable pageable
    );

    long countByStatus(StatusPedidoEnum status);

}
