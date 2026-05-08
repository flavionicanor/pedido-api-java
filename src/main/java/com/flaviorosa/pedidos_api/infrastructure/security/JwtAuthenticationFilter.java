package com.flaviorosa.pedidos_api.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // sem token: passa para o próximo filtro
        // o Spring Security vai bloquear se o endpoint exigir autenticação
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // passa pra frente sem autenticar
            return; // sai do filtro
        }

        String token = authHeader.substring(7); // remove "Bearer "

        if(jwtService.isValido(token)){
            String clienteId = jwtService.extrairClienteId(token);
            List<String> perfis = jwtService.extrairPerfis(token);

            var authorities = perfis.stream()
                    .map(p -> new SimpleGrantedAuthority("ROLE_" +p))
                    .toList();

            var auth = new UsernamePasswordAuthenticationToken(clienteId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

}
