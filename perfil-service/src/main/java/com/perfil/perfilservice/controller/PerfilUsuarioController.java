package com.perfil.perfilservice.controller;

import com.perfil.perfilservice.ConfigSecurity.FileStorageService;
import com.perfil.perfilservice.dto.*;
import com.perfil.perfilservice.service.PerfilUsuarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/perfil")
@RequiredArgsConstructor
public class PerfilUsuarioController {

    @Autowired
    private final PerfilUsuarioService perfilUsuarioService;



    @Autowired
    private FileStorageService fileStorageService;


    @PostMapping("/foto")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(@RequestHeader("username") Long authUserId, @RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file, authUserId);
        String fileDownloadUri = "/uploads/profile-images/" + fileName; // Ruta relativa para el frontend

        perfilUsuarioService.updateFotoPerfil(authUserId, fileDownloadUri);

        Map<String, String> response = new HashMap<>();
        response.put("url", fileDownloadUri);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/completo")
    public ResponseEntity<PerfilCompletoDTO> getPerfilCompleto(@RequestHeader("username") Long authUserId) {
        PerfilCompletoDTO perfilCompleto = perfilUsuarioService.getPerfilCompleto(authUserId);
        return ResponseEntity.ok(perfilCompleto);
    }

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
    public ResponseEntity<?> obtenerMiPerfil(
            Authentication authentication,
            @RequestHeader("username") String email,
            @RequestHeader("X-User-Id") Long userId) { // <-- Capturamos el ID directamente como Long

        try {
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID de usuario no encontrado en la cabecera X-User-Id"));
            }

            // Ya tenemos el userId como Long y el email como String, listos para usar.
            Optional<PerfilUsuarioDTO> perfil = perfilUsuarioService.obtenerOCrearPerfil(userId, email);

            if(perfil.isPresent()){
                return ResponseEntity.ok(perfil.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "No se pudo encontrar o crear el perfil."));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor al obtener el perfil: " + e.getMessage()));
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

    @PostMapping("/mi-perfil/foto")
    public ResponseEntity<Map<String, String>> actualizarFotoPerfil(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("file") MultipartFile file) {

        // CORRECCIÓN 1: Se llama al método correcto 'storeFile'.
        // CORRECCIÓN 2: Se pasan los dos argumentos que requiere: el archivo y el ID del usuario.
        String fileName = fileStorageService.storeFile(file, userId);

        // Construimos la ruta relativa que se guardará en la base de datos.
        String fileUrl = "/uploads/profile-images/" + fileName;

        // Actualizamos el perfil del usuario con la nueva ruta de la imagen.
        perfilUsuarioService.actualizarUrlFoto(userId, fileUrl);

        // Devolvemos la nueva ruta al frontend para que pueda mostrar la imagen.
        return ResponseEntity.ok(Map.of("fotoPerfilUrl", fileUrl));
    }

    @DeleteMapping("/mi-perfil/foto")
    public ResponseEntity<Void> eliminarFotoPerfil(
            @RequestHeader("X-User-Id") Long userId) {

        // CORRECCIÓN: Usar la instancia 'perfilUsuarioService' para poner la URL a null.
        perfilUsuarioService.actualizarUrlFoto(userId, null);

        // Devuelve una respuesta 204 No Content, indicando que la operación fue exitosa.
        return ResponseEntity.noContent().build();
    }

}

