package com.gateway.authenticationservice.Controller;


import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter

public class RegisterRequest {
    private String firstname;

    private String lastname;

    private String email;

    private String password;

}
