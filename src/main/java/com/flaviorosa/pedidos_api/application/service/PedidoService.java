package com.flaviorosa.pedidos_api.application.service;

import com.flaviorosa.pedidos_api.domain.exception.PedidoNaoEncontradoException;
import com.flaviorosa.pedidos_api.domain.exception.TransicaoEstadoInvalidaException;
import com.flaviorosa.pedidos_api.domain.model.Item;
import com.flaviorosa.pedidos_api.domain.model.Pedido;
import com.flaviorosa.pedidos_api.infrastructure.persistence.entity.PedidoEntity;
import com.flaviorosa.pedidos_api.infrastructure.persistence.entity.StatusPedidoEnum;
import com.flaviorosa.pedidos_api.infrastructure.persistence.mapper.PedidoMapper;
import com.flaviorosa.pedidos_api.infrastructure.persistence.repository.PedidoRepository;
import com.flaviorosa.pedidos_api.infrastructure.web.dto.CriarPedidoRequest;
import com.flaviorosa.pedidos_api.infrastructure.web.dto.PedidoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j  // injeta o logger: log.info(), log.error(), etc.
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository repository;
    private final PedidoMapper mapper;

    @Transactional
    public PedidoResponse criar(CriarPedidoRequest request){
        log.info("Criando pedido para cliente: {}", request.clienteId());

        List<Item> itens = request.itens().stream()
                .map(i -> new Item(i.produtoId(), i.nome(),
                        i.quantidade(), i.precoUnitario()))
                .toList();

        Pedido pedido = new Pedido(request.clienteId(), itens);

        // save() retorna a entidade com o ID gerado pelo @GeneratedValue
        PedidoEntity salvo = repository.save(mapper.toEntity(pedido));

        log.info("Pedido criado com ID: {}", salvo.getId());
        return PedidoResponse.from(mapper.toDomain(salvo));
    }

    // readOnly = true: Spring não faz dirty checking — mais performance em leituras
    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(String id) {
        // findWithItensById usa @EntityGraph — resolve N+1 automaticamente
        return repository.findWithItensById(id)
                .map(mapper::toDomain)
                .map(PedidoResponse::from)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponse> listar(String clienteId, String status, Pageable pageable) {
        StatusPedidoEnum statusEnum = status != null
                ? StatusPedidoEnum.valueOf(status.toUpperCase()) : null;

        return repository.findByFiltros(clienteId, statusEnum, pageable)
                .map(mapper::toDomain)
                .map(PedidoResponse::from);
    }

    @Transactional
    public PedidoResponse processar(String id, String responsavel){
        log.info("Processando pedido: {} por {}", id, responsavel);

        Pedido pedido = buscarDominio(id);

        try{
            pedido.processar(responsavel);
        } catch (IllegalStateException e) {
            throw new TransicaoEstadoInvalidaException(e.getMessage());
        }

        repository.save(mapper.toEntity(pedido));
        return PedidoResponse.from(pedido);

    }

    @Transactional
    public PedidoResponse concluir(String id) {
        log.info("Concluindo pedido: {}", id);

        Pedido pedido = buscarDominio(id);

        try{
            pedido.concluir();
        } catch (IllegalStateException e) {
            throw new TransicaoEstadoInvalidaException(e.getMessage());
        }

        repository.save(mapper.toEntity(pedido));
        return PedidoResponse.from(pedido);
    }

    @Transactional
    public void cancelar(String id, String motivo) {
        log.info("Cancelando pedido: {} motivo: {}", id, motivo);

        Pedido pedido = buscarDominio(id);

        try{
            pedido.cancelar(motivo);
        } catch (IllegalStateException e) {
            throw new TransicaoEstadoInvalidaException(e.getMessage());
        }

        repository.save(mapper.toEntity(pedido));
    }

    // método privado — reutilizado internamente, não exposto
    private Pedido buscarDominio(String id){
        return repository.findWithItensById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));

    }

    public boolean pertenceAoCliente(String pedidoId, String clienteId) {
        return repository.findById(pedidoId)
                .map(p -> p.getClienteId().equals(clienteId))
                .orElse(false);
    }

}
