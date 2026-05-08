package com.flaviorosa.pedidos_api.infrastructure.web.controller;

import com.flaviorosa.pedidos_api.application.service.PedidoService;
import com.flaviorosa.pedidos_api.infrastructure.security.JwtService;
import com.flaviorosa.pedidos_api.infrastructure.web.dto.CriarPedidoRequest;
import com.flaviorosa.pedidos_api.infrastructure.web.dto.PedidoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final JwtService jwt;

    // POST /api/v1/pedidos
    // @Valid dispara a validação do Bean Validation no request body
    @PostMapping
    public ResponseEntity<PedidoResponse> criar(
            @RequestBody @Valid CriarPedidoRequest request){
        PedidoResponse response = pedidoService.criar(request);

        // 201 Created com o Location header apontando para o recurso criado
        URI location = URI.create("/api/v1/pedidos/"+ response.id());
        return ResponseEntity.created(location).body(response);
    }

    // GET /api/v1/pedidos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable String id){
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    // GET /api/v1/pedidos?clienteId=X&status=AGUARDANDO&page=0&size=10&sort=criadoEm,desc
    @GetMapping
    public ResponseEntity<Page<PedidoResponse>> listar(
            @RequestParam(required = false) String clienteId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "criadoEm") Pageable pageable) {

        return ResponseEntity.ok(pedidoService.listar(clienteId, status, pageable));
    }



    // PATCH /api/v1/pedidos/{id}/processar
    @PatchMapping("/{id}/processar")
    public ResponseEntity<PedidoResponse> processar(
            @PathVariable String id,
            @RequestParam String responsavel){
        return ResponseEntity.ok(pedidoService.processar(id, responsavel));
    }

    // PATCH /api/v1/pedidos/{id}/concluir
    @PatchMapping("/{id}/concluir")
    public ResponseEntity<PedidoResponse> concluir(@PathVariable String id) {
        return ResponseEntity.ok(pedidoService.concluir(id));
    }

    // DELETE /api/v1/pedidos/{id}?motivo=desistência
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(
            @PathVariable String id,
            @RequestParam String motivo) {

        pedidoService.cancelar(id, motivo);
        return ResponseEntity.noContent().build();
    }

}
