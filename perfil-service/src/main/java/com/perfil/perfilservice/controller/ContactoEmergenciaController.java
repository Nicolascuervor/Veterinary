package com.perfil.perfilservice.controller;

import com.perfil.perfilservice.dto.ContactoEmergenciaDTO;
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

@RestController
@RequestMapping("/perfil/contactos-emergencia")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContactoEmergenciaController {

    private final PerfilUsuarioService perfilUsuarioService;

    // Obtener mis contactos de emergencia
    @GetMapping
    public ResponseEntity<?> obtenerMisContactos(Authentication authentication) {
        try {
            String userId = (String) authentication.getDetails();
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID de usuario no encontrado"));
            }

            List<ContactoEmergenciaDTO> contactos = perfilUsuarioService
                    .obtenerContactosEmergencia(Long.valueOf(userId));

            return ResponseEntity.ok(Map.of(
                    "contactos", contactos,
                    "total", contactos.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener contactos"));
        }
    }

    // Agregar contacto de emergencia
    @PostMapping
    public ResponseEntity<?> agregarContacto(
            @Valid @RequestBody ContactoEmergenciaDTO contactoDTO,
            Authentication authentication) {
        try {
            String userId = (String) authentication.getDetails();
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID de usuario no encontrado"));
            }

            ContactoEmergenciaDTO contactoCreado = perfilUsuarioService
                    .agregarContactoEmergencia(Long.valueOf(userId), contactoDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensaje", "Contacto de emergencia agregado exitosamente",
                    "contacto", contactoCreado
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al agregar contacto"));
        }
    }
}
