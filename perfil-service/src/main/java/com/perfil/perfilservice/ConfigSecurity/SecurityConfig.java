package com.perfil.perfilservice.ConfigSecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
