package com.agend.agendamientoservice.ConfigSecurity;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                        .requestMatchers("/veterinarios/registrar").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/propietarios/registrar").permitAll()
                        .requestMatchers("/servicios/registrar").permitAll()


                        .requestMatchers("/servicios").permitAll()
                        .requestMatchers("/propietarios/**").permitAll()
                        .requestMatchers("/veterinarios/**").permitAll()
                        .requestMatchers("/AreaClinica/**").permitAll()
                        .requestMatchers("/mascotas/**").permitAll()
                        .requestMatchers("/disponibilidad/**").permitAll()
                        .requestMatchers("/agenda/**").permitAll()
                        .requestMatchers("/cita/**").permitAll()
                        .requestMatchers("/historial/**").permitAll()
                        .requestMatchers("/imagenes/mascotas/**").permitAll()


                        .requestMatchers(HttpMethod.GET, "/propietarios/{id}").authenticated()




                        .requestMatchers("/**").hasAuthority("ROLE_ADMIN")






                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
