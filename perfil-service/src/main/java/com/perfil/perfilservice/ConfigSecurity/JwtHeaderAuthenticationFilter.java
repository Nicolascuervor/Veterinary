package com.perfil.perfilservice.ConfigSecurity;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
class JwtHeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String username = request.getHeader("username");
        String rol = request.getHeader("role");
        String userId = request.getHeader("X-User-Id");

        System.out.println("🛡️ Filtro de seguridad en perfil-service activado");
        System.out.println("➡️ Headers recibidos:");
        System.out.println(" - username: " + username);
        System.out.println(" - role: " + rol);
        System.out.println(" - userId: " + userId);

        if (username != null && rol != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(rol);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, List.of(authority));

            // Agregar el userId como detalle adicional
            authentication.setDetails(userId);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("✅ Contexto de seguridad establecido correctamente");
        } else {
            System.out.println("❌ No se estableció autenticación (headers faltantes o ya autenticado)");
        }

        filterChain.doFilter(request, response);
    }
}

@Configuration
@EnableWebSecurity
class SecurityConfig {

    private final JwtHeaderAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtHeaderAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/perfil/publico/**").permitAll()

                        // Endpoints que requieren autenticación
                        .requestMatchers("/perfil/**").authenticated()

                        // Endpoints específicos por rol
                        .requestMatchers("/perfil/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/perfil/veterinario/**").hasAuthority("ROLE_VETERINARIO")

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
