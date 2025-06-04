package com.perfil.perfilservice.controller;

import com.perfil.perfilservice.dto.*;
import com.perfil.perfilservice.service.PerfilUsuarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/perfil")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PerfilUsuarioController {

    private final PerfilUsuarioService perfilUsuarioService;

    // Crear perfil de usuario
    @PostMapping("/crear")
    public ResponseEntity<?> crearPerfil(@Valid @RequestBody PerfilUsuarioCreateDTO createDTO) {
        try {
            PerfilUsuarioDTO perfilCreado = perfilUsuarioService.crearPerfil(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensaje", "Perfil creado exitosamente",
                    "perfil", perfilCreado
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // Obtener mi perfil (usuario autenticado)
    @GetMapping("/mi-perfil")
    public ResponseEntity<?> obtenerMiPerfil(Authentication authentication) {
        try {
            String userId = (String) authentication.getDetails();
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID de usuario no encontrado"));
            }

            Optional<PerfilUsuarioDTO> perfil = perfilUsuarioService.obtenerPerfilPorUserId(Long.valueOf(userId));

            if (perfil.isPresent()) {
                return ResponseEntity.ok(perfil.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Perfil no encontrado"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener el perfil"));
        }
    }

    // Obtener perfil por ID (solo admins o el mismo usuario)
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPerfilPorId(@PathVariable Long id, Authentication authentication) {
        try {
            Optional<PerfilUsuarioDTO> perfil = perfilUsuarioService.obtenerPerfilPorId(id);

            if (perfil.isPresent()) {
                return ResponseEntity.ok(perfil.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "Perfil no encontrado"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener el perfil"));
        }
    }

    // Actualizar mi perfil
    @PutMapping("/actualizar")
    public ResponseEntity<?> actualizarMiPerfil(
            @Valid @RequestBody PerfilUsuarioUpdateDTO updateDTO,
            Authentication authentication) {
        try {
            String userId = (String) authentication.getDetails();
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID de usuario no encontrado"));
            }

            PerfilUsuarioDTO perfilActualizado = perfilUsuarioService.actualizarPerfil(
                    Long.valueOf(userId), updateDTO);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Perfil actualizado exitosamente",
                    "perfil", perfilActualizado
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar el perfil"));
        }
    }

    // Eliminar mi perfil
    @DeleteMapping("/eliminar")
    public ResponseEntity<?> eliminarMiPerfil(Authentication authentication) {
        try {
            String userId = (String) authentication.getDetails();
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID de usuario no encontrado"));
            }

            perfilUsuarioService.eliminarPerfil(Long.valueOf(userId));
            return ResponseEntity.ok(Map.of("mensaje", "Perfil eliminado exitosamente"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar el perfil"));
        }
    }

    // Buscar perfiles (público)
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPerfiles(@RequestParam String termino) {
        try {
            if (termino.trim().length() < 2) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El término de búsqueda debe tener al menos 2 caracteres"));
            }

            List<PerfilResumenDTO> perfiles = perfilUsuarioService.buscarPerfiles(termino);
            return ResponseEntity.ok(Map.of(
                    "perfiles", perfiles,
                    "total", perfiles.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error en la búsqueda"));
        }
    }
}

