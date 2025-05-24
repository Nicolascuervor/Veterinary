package com.gateway.authenticationservice.Controller;


import com.gateway.authenticationservice.model.Role;
import lombok.Data;

@Data
class RegisterRequest {
    private String username;
    private String password;
    private Role role;
}
