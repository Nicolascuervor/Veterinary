package com.gateway.authenticationservice.DTOs;



import com.gateway.authenticationservice.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAdminViewDTO {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean isEnabled;

}