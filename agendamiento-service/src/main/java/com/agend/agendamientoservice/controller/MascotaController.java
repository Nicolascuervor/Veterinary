package com.agend.agendamientoservice.controller;

import com.agend.agendamientoservice.DTOs.UserDTO;
import com.agend.agendamientoservice.Emails.EmailClient;
import com.agend.agendamientoservice.model.Mascota;
import com.agend.agendamientoservice.model.Propietario;
import com.agend.agendamientoservice.repository.PropietarioRepository;
import com.agend.agendamientoservice.service.FileStorageService;
import com.agend.agendamientoservice.service.MascotaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final EmailClient emailClient;
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(MascotaController.class);

    @Autowired private MascotaService mascotaService;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private PropietarioRepository propietarioRepository;
    @Autowired private ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<Mascota> guardarMascota(
            @RequestParam("mascota") String mascotaJson,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpServletRequest httpRequest) throws IOException {

        logger.info("==================================================================");
        logger.info("INICIO DE PROCESO - REGISTRO DE MASCOTA");
        logger.info("==================================================================");
        logger.info("LOG #1 - JSON Crudo Recibido: {}", mascotaJson);

        Mascota mascota = objectMapper.readValue(mascotaJson, Mascota.class);
        logger.info("LOG #2 - JSON deserializado a objeto Mascota (propietario aún no asignado).");

        JsonNode rootNode = objectMapper.readTree(mascotaJson);
        if (!rootNode.has("propietario") || !rootNode.get("propietario").has("id")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo 'propietario' con su 'id' es requerido en el JSON.");
        }
        Long propietarioId = rootNode.get("propietario").get("id").asLong();
        logger.info("LOG #3 - ID de propietario extraído del JSON: {}", propietarioId);

        Propietario propietario = propietarioRepository.findById(propietarioId)
                .orElseThrow(() -> new EntityNotFoundException("Propietario no encontrado con ID: " + propietarioId));

        mascota.setPropietario(propietario);
        logger.info("LOG #4 - Propietario encontrado y asignado exitosamente: {}", propietario.getNombre());

        if (file != null && !file.isEmpty()) {
            String fileUrl = fileStorageService.save(file);
            mascota.setImageUrl(fileUrl);
            logger.info("LOG #5 - Archivo de imagen guardado en la URL: {}", fileUrl);
        }

        Mascota mascotaGuardada = mascotaService.guardar(mascota);
        logger.info("LOG #6 - Mascota guardada en la BD con ID: {}", mascotaGuardada.getId());

        // =======================================================================
        // ▼▼▼         AQUÍ COMIENZA LA IMPLEMENTACIÓN DEL ENVÍO DE CORREO         ▼▼▼
        // =======================================================================

        // Paso 1: Obtener el token de autorización de la cabecera.
        final String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Si no hay token, no se puede continuar para obtener el email.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta el token de autorización.");
        }

        // Paso 2: Llamar al servicio de autenticación para obtener el email del usuario.
        UserDTO usuario = obtenerDatosDeUsuario(mascotaGuardada.getPropietario().getUsuarioId(), authHeader);

        // Paso 3: Verificar que se obtuvo el usuario y su email.
        if (usuario != null && usuario.getEmail() != null) {
            try {
                // Paso 4: Preparar y enviar el correo.
                String nombrePropietario = mascotaGuardada.getPropietario().getNombre();
                String nombreMascota = mascotaGuardada.getNombre();
                String asunto = "¡Bienvenido a VetCare, " + nombreMascota + "!";
                String mensaje = String.format(
                        "Hola %s,\n\n" +
                                "¡Estamos encantados de dar la bienvenida a tu querida mascota, %s, a la familia VetCare! 🐶🐱\n\n" +
                                "Gracias por confiar en nosotros.\n\n" +
                                "El equipo de VetCare 🐾",
                        nombrePropietario, nombreMascota
                );

                emailClient.enviarEmail(new String[]{usuario.getEmail()}, asunto, mensaje);
                logger.info("LOG #7 - Correo de bienvenida enviado a: {}", usuario.getEmail());

            } catch (Exception e) {
                // Si el envío falla, solo se registra en logs, pero no se anula el registro de la mascota.
                logger.error("La mascota se creó, pero falló el envío del correo de bienvenida. Causa: " + e.getMessage());
            }
        } else {
            logger.error("No se pudieron obtener los datos del usuario para el envío de correo. No se envió la notificación.");
        }

        // =======================================================================
        // ▲▲▲          AQUÍ TERMINA LA IMPLEMENTACIÓN DEL ENVÍO DE CORREO         ▲▲▲
        // =======================================================================

        logger.info("==================================================================");
        logger.info("¡ÉXITO! Proceso de registro de mascota finalizado.");
        logger.info("==================================================================");

        return ResponseEntity.status(HttpStatus.CREATED).body(mascotaGuardada);
    }

    // --- OTROS MÉTODOS (SIN CAMBIOS) ---

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> actualizarMascota(
            @PathVariable Long id,
            @RequestParam("mascota") String mascotaJson,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        Mascota mascotaExistente = mascotaService.findMascotaById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mascota no encontrada con ID: " + id));

        Mascota datosActualizados = objectMapper.readValue(mascotaJson, Mascota.class);

        mascotaExistente.setNombre(datosActualizados.getNombre());
        mascotaExistente.setEspecie(datosActualizados.getEspecie());
        mascotaExistente.setRaza(datosActualizados.getRaza());
        mascotaExistente.setEdad(datosActualizados.getEdad());
        mascotaExistente.setPeso(datosActualizados.getPeso());
        mascotaExistente.setColor(datosActualizados.getColor());
        mascotaExistente.setFechaNacimiento(datosActualizados.getFechaNacimiento());

        if (file != null && !file.isEmpty()) {
            String fileUrl = fileStorageService.save(file);
            mascotaExistente.setImageUrl(fileUrl);
        }

        Mascota mascotaActualizada = mascotaService.guardar(mascotaExistente);
        return ResponseEntity.ok(mascotaActualizada);
    }

    private UserDTO obtenerDatosDeUsuario(Long usuarioId, String token) {
        // La URL debe apuntar al nombre del servicio si se usa Docker/Kubernetes
        String url = "http://localhost:8082/auth/internal/user/" + usuarioId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<UserDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserDTO.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error al comunicarse con authentication-service en {}. Causa: {}", url, e.getMessage());
            return null;
        }
    }

    @GetMapping
    public List<Mascota> findAll() { return mascotaService.findAllMascotas(); }

    @GetMapping("/propietario/{propietarioId}")
    public ResponseEntity<List<Mascota>> findByPropietarioId(@PathVariable Long propietarioId) {
        List<Mascota> mascotas = mascotaService.findByPropietarioId(propietarioId);
        if (mascotas.isEmpty()) { return ResponseEntity.noContent().build(); }
        return ResponseEntity.ok(mascotas);
    }

    @GetMapping("/{id}")
    public Optional<Mascota> findMascotaById(@PathVariable Long id) { return mascotaService.findMascotaById(id); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarMascota(@PathVariable Long id) {
        mascotaService.eliminar(id);
        return ResponseEntity.ok(Map.of("eliminado", true));
    }
}