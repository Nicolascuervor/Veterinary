package com.perfil.perfilservice.controller;

import com.perfil.perfilservice.dto.EstadisticasPerfilDTO;
import com.perfil.perfilservice.service.PerfilUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/perfil/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PerfilAdminController {

    private final PerfilUsuarioService perfilUsuarioService;

    // Obtener estadísticas de perfiles (solo admin)
    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas(Authentication authentication) {
        try {
            // Verificar que el usuario tenga rol de admin
            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!esAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado. Se requieren privilegios de administrador"));
            }

            EstadisticasPerfilDTO estadisticas = perfilUsuarioService.obtenerEstadisticas();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener estadísticas"));
        }
    }

    // Obtener todos los perfiles (solo admin)
    @GetMapping("/todos")
    public ResponseEntity<?> obtenerTodosLosPerfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        try {
            // Verificar que el usuario tenga rol de admin
            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!esAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado"));
            }

            // Aquí deberías implementar paginación en el servicio
            // Por ahora devolvemos un mensaje indicativo
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Funcionalidad de listado paginado pendiente de implementar",
                    "page", page,
                    "size", size
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener perfiles"));
        }
    }
}
