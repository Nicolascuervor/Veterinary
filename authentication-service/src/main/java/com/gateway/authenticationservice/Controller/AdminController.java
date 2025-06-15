package com.gateway.authenticationservice.Controller;

import com.gateway.authenticationservice.DTOs.UserAdminViewDTO;
import com.gateway.authenticationservice.model.Role;
import com.gateway.authenticationservice.model.User;
import com.gateway.authenticationservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import org.slf4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);


    @GetMapping("/users")
    public ResponseEntity<List<UserAdminViewDTO>> getAllUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Accediendo a /api/admin/users. Usuario: '{}', Roles: {}", authentication.getName(), authentication.getAuthorities());

        List<UserAdminViewDTO> users = userService.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean isEnabled = body.get("enabled");
        return userService.updateUserStatus(id, isEnabled)
                .map(user -> ResponseEntity.ok().body("Estado del usuario actualizado correctamente."))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')") // Solo el ADMIN puede cambiar roles
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Role newRole = Role.valueOf(body.get("role").toUpperCase());
            return userService.updateUserRole(id, newRole)
                    .map(user -> ResponseEntity.ok().body("Rol del usuario actualizado correctamente."))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("El rol proporcionado no es válido.");
        }
    }

    private UserAdminViewDTO convertToDto(User user) {
        return UserAdminViewDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getUsername())
                .role(user.getRole())
                .isEnabled(user.isEnabled())
                .build();
    }
}