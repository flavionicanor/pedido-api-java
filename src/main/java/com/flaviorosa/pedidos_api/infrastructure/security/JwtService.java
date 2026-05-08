package com.flaviorosa.pedidos_api.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private long expiration;

    // executa após o Spring injetar os @Value — valida na inicialização
    @PostConstruct
    public void validarConfiguracao() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "security.jwt.secret não configurado no application.yml");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException(
                    "security.jwt.secret precisa ter no mínimo 32 caracteres");
        }
        log.info("JwtService iniciado. Secret configurado: {} chars", secret.length());
    }

    private SecretKey getChave() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(String clienteId, List<String> perfis) {
        log.debug("Gerando token para clienteId: {}", clienteId);

        return Jwts.builder()
                .subject(clienteId)
                .claim("perfis", perfis)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getChave())
                .compact();
    }

    public String extrairClienteId(String token) {
        return extrairClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extrairPerfis(String token) {
        return (List<String>) extrairClaims(token).get("perfis");
    }

    public boolean isValido(String token) {
        try {
            extrairClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(getChave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
