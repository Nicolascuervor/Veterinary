package com.gateway.authenticationservice.Controller;


import com.gateway.authenticationservice.model.Role;
import lombok.Data;

@Data
class RegisterRequest {
    private String username;
    private String password;
    private String nombre;
    private String apellido;
    private String telefono;
    private String direccion;
    private String cedula;
    private Role role;
}



