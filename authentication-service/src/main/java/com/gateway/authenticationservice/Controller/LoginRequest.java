package com.gateway.authenticationservice.Controller;

import lombok.Data;

@Data
class LoginRequest {
    private String username;
    private String password;
}
