package com.perfil.perfilservice.controller;

import com.perfil.perfilservice.dto.PreferenciaPrivacidadDTO;
import com.perfil.perfilservice.service.PerfilUsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/perfil/preferencias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PreferenciaPrivacidadController {

    private final PerfilUsuarioService perfilUsuarioService;

    // Obtener mis preferencias de privacidad
    @GetMapping
    public ResponseEntity<?> obtenerMisPreferencias(Authentication authentication) {
        try {
            String userId = (String) authentication.getDetails();
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID de usuario no encontrado"));
            }

            List<PreferenciaPrivacidadDTO> preferencias = perfilUsuarioService
                    .obtenerPreferenciasPrivacidad(Long.valueOf(userId));

            return ResponseEntity.ok(Map.of(
                    "preferencias", preferencias,
                    "total", preferencias.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener preferencias"));
        }
    }

    // Actualizar preferencia específica
    @PutMapping("/{tipoPreferencia}")
    public ResponseEntity<?> actualizarPreferencia(
            @PathVariable String tipoPreferencia,
            @RequestBody Map<String, Boolean> request,
            Authentication authentication) {
        try {
            String userId = (String) authentication.getDetails();
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID de usuario no encontrado"));
            }

            Boolean valor = request.get("valor");
            if (valor == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El valor de la preferencia es obligatorio"));
            }

            PreferenciaPrivacidadDTO preferenciaActualizada = perfilUsuarioService
                    .actualizarPreferencia(Long.valueOf(userId), tipoPreferencia, valor);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Preferencia actualizada exitosamente",
                    "preferencia", preferenciaActualizada
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar preferencia"));
        }
    }
}
