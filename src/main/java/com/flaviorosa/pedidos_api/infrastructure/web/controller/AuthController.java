package com.flaviorosa.pedidos_api.infrastructure.web.controller;

import com.flaviorosa.pedidos_api.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    // GET /api/v1/auth/token/{clienteId}
    // rota temporária para testes — em produção seria POST com login/senha
    @GetMapping("/token/{clienteId}")
    public Map<String, String> gerarToken(@PathVariable String clienteId) {
        String token = jwtService.gerarToken(clienteId, List.of("USER"));
        return Map.of("token", token);
    }
}
