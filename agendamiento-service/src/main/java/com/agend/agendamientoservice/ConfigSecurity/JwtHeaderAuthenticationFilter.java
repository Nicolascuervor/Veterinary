package com.agend.agendamientoservice.ConfigSecurity;


import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

@Component
public class JwtHeaderAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtHeaderAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println("🛡️ Filtro de seguridad en microservicio activado");

        Enumeration<String> headers = request.getHeaderNames();
        System.out.println("📦 Headers recibidos en agendamiento-service:");
        while (headers.hasMoreElements()) {
            String name = headers.nextElement();
            System.out.println(" - " + name + ": " + request.getHeader(name));
        }


        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ Header Authorization no encontrado o mal formado");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // quitar "Bearer "

        try {
            Claims claims = jwtService.extractAllClaims(token);
            String username = claims.getSubject();
            String role = claims.get("authorities", String.class);

            System.out.println("✅ JWT decodificado:");
            System.out.println(" - username: " + username);
            System.out.println(" - role: " + role);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(username, null, Collections.singleton(authority));

                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("✅ Contexto de seguridad establecido correctamente");
            }

        } catch (Exception e) {
            System.out.println("❌ Error al procesar JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
