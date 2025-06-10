package com.gateway.authenticationservice.Controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    private String username;
    private String nombre; // ✅ Agregado para mostrar el nombre del propietario
    private Long id; 

   
}
