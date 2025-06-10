package com.gateway.apigateway.filtro;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("🌐 Gateway interceptó la solicitud");

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("🔐 JWT recibido: " + token);

            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();
                // ✅ Usa el authority tal como está en el token (ya incluye ROLE_)
                String rol = claims.get("authorities").toString();

                String id = String.valueOf(claims.get("usuarioId"));

                System.out.println("✅ Claims parseados correctamente:");
                System.out.println(" - username: " + username);
                System.out.println(" - role: " + rol);
                System.out.println(" - id: " + id);

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(r -> r.headers(h -> {
                            h.add("username", username);
                            h.add("role", rol);
                            h.add("x-user-Id", id);
                        }))
                        .build();

                System.out.println("📦 Headers agregados a la request");

                return chain.filter(modifiedExchange);

            } catch (Exception e) {
                System.out.println("❌ Error al validar JWT: " + e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        System.out.println("⚠️ No se recibió Authorization header válido");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
