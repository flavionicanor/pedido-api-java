package com.flaviorosa.pedidos_api.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository repository;
    private final PedidoMapper mapper;
    private final StringRedisTemplate redisTemplate;  // já auto-configurado pelo Spring Boot
    private final ObjectMapper objectMapper;           // já auto-configurado pelo Spring Boot

    private static final String CACHE_PREFIX = "pedidos:";
    private static final Duration CACHE_TTL  = Duration.ofMinutes(5);

    @Transactional
    public PedidoResponse criar(CriarPedidoRequest request) {
        log.info("Criando pedido para cliente: {}", request.clienteId());

        List<Item> itens = request.itens().stream()
                .map(i -> new Item(i.produtoId(), i.nome(),
                        i.quantidade(), i.precoUnitario()))
                .toList();

        Pedido pedido = new Pedido(request.clienteId(), itens);
        PedidoEntity salvo = repository.save(mapper.toNewEntity(pedido));

        log.info("Pedido criado com ID: {}", salvo.getId());
        return PedidoResponse.from(mapper.toDomain(salvo));
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(String id) {
        // 1. tenta buscar no Redis primeiro
        String cacheKey = CACHE_PREFIX + id;
        String json = redisTemplate.opsForValue().get(cacheKey);

        if (json != null) {
            log.debug("Cache HIT — pedido {} veio do Redis", id);
            try {
                return objectMapper.readValue(json, PedidoResponse.class);
            } catch (IOException e) {
                // se o cache está corrompido, ignora e busca no banco
                log.warn("Cache corrompido para pedido {}, buscando no banco", id);
            }
        }

        // 2. não estava no cache — busca no banco
        log.debug("Cache MISS — buscando pedido {} no banco", id);
        PedidoResponse response = repository.findWithItensById(id)
                .map(mapper::toDomain)
                .map(PedidoResponse::from)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));

        // 3. salva no Redis para a próxima consulta
        salvarNoCache(cacheKey, response);

        return response;
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
    public PedidoResponse processar(String id, String responsavel) {
        log.info("Processando pedido: {} por {}", id, responsavel);
        Pedido pedido = buscarDominio(id);
        try {
            pedido.processar(responsavel);
        } catch (IllegalStateException e) {
            throw new TransicaoEstadoInvalidaException(e.getMessage());
        }
        repository.save(mapper.toEntity(pedido));
        invalidarCache(id);  // remove do Redis — próxima busca virá do banco atualizado
        return PedidoResponse.from(pedido);
    }

    @Transactional
    public PedidoResponse concluir(String id) {
        log.info("Concluindo pedido: {}", id);
        Pedido pedido = buscarDominio(id);
        try {
            pedido.concluir();
        } catch (IllegalStateException e) {
            throw new TransicaoEstadoInvalidaException(e.getMessage());
        }
        repository.save(mapper.toEntity(pedido));
        invalidarCache(id);
        return PedidoResponse.from(pedido);
    }

    @Transactional
    public void cancelar(String id, String motivo) {
        log.info("Cancelando pedido: {} motivo: {}", id, motivo);
        Pedido pedido = buscarDominio(id);
        try {
            pedido.cancelar(motivo);
        } catch (IllegalStateException e) {
            throw new TransicaoEstadoInvalidaException(e.getMessage());
        }
        repository.save(mapper.toEntity(pedido));
        invalidarCache(id);
    }

    private Pedido buscarDominio(String id) {
        return repository.findWithItensById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }

    public boolean pertenceAoCliente(String pedidoId, String clienteId) {
        return repository.findById(pedidoId)
                .map(p -> p.getClienteId().equals(clienteId))
                .orElse(false);
    }

    // salva o objeto como JSON puro no Redis
    private void salvarNoCache(String cacheKey, PedidoResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL);
            log.debug("Pedido salvo no cache: {}", cacheKey);
        } catch (JsonProcessingException e) {
            // falha no cache não deve derrubar a operação principal
            log.warn("Não foi possível salvar no cache: {}", e.getMessage());
        }
    }

    // remove a entrada do Redis quando o pedido é alterado
    private void invalidarCache(String id) {
        redisTemplate.delete(CACHE_PREFIX + id);
        log.debug("Cache invalidado para pedido: {}", id);
    }
}