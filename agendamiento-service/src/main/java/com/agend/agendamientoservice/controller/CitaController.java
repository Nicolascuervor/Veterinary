package com.agend.agendamientoservice.controller;

import com.agend.agendamientoservice.DTOs.CitaRequest;
import com.agend.agendamientoservice.DTOs.CitaResponseDTO;
import com.agend.agendamientoservice.DTOs.UserDTO;
import com.agend.agendamientoservice.model.Cita;
import com.agend.agendamientoservice.model.Mascota;
import com.agend.agendamientoservice.model.Propietario;
import com.agend.agendamientoservice.model.Veterinario;
import com.agend.agendamientoservice.repository.MascotaRepository;
import com.agend.agendamientoservice.repository.VeterinarioRepository;
import com.agend.agendamientoservice.service.CitaService;
import com.agend.agendamientoservice.Emails.EmailClient; // Asegúrate que esta clase exista y esté configurada.


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@RestController
@RequestMapping("/cita")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;
    private final EmailClient emailClient;
    private final RestTemplate restTemplate;

    private final MascotaRepository mascotaRepository;
    private final VeterinarioRepository veterinarioRepository;

    @PostMapping("/registrar")
    public ResponseEntity<CitaResponseDTO> guardarCita(@RequestBody CitaRequest request, HttpServletRequest httpRequest) {

        // 1. Crear la cita y obtener el DTO de respuesta.
        // Esto valida y guarda la cita en la base de datos.
        CitaResponseDTO nuevaCitaDTO = citaService.crearCitaDesdeRequest(request);

        // 2. Obtener los datos necesarios para el email usando los IDs del 'request' original.
        try {
            final String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta el token de autorización.");
            }

            // Obtenemos las entidades completas para construir el email
            Mascota mascota = mascotaRepository.findById(request.getMascotaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada para la notificación."));

            Propietario propietario = mascota.getPropietario();
            if (propietario == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "La mascota no tiene un propietario asociado.");
            }

            UserDTO usuario = obtenerDatosDeUsuario(propietario.getUsuarioId(), authHeader);
            if (usuario == null || usuario.getEmail() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se pudieron obtener los datos del usuario para la notificación.");
            }

            // Formateamos los datos para el email
            String nombrePropietario = propietario.getNombre();
            String nombreMascota = mascota.getNombre();
            String nombreVeterinario = nuevaCitaDTO.getVeterinarioNombre(); // Podemos usar el nombre del DTO

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy 'a las' HH:mm 'horas'");
            String fechaFormateada = LocalDateTime.of(nuevaCitaDTO.getFecha(), nuevaCitaDTO.getHora()).format(formatter);

            String asunto = "Confirmación de Cita para " + nombreMascota;
            String mensaje = String.format(
                    "Hola %s,\n\n" +
                            "Te confirmamos que tu cita para %s ha sido agendada con éxito.\n\n" +
                            "Detalles de la cita:\n" +
                            "  - Fecha y Hora: %s\n" +
                            "  - Veterinario: Dr. %s\n" +
                            "  - Motivo: %s\n\n" +
                            "Gracias por confiar en VetCare.",
                    nombrePropietario, nombreMascota, fechaFormateada, nombreVeterinario, nuevaCitaDTO.getMotivo()
            );

            emailClient.enviarEmail(new String[]{usuario.getEmail()}, asunto, mensaje);

        } catch (Exception e) {
            // Si el envío del correo falla, no se anula la cita, solo se registra el error.
            // La cita ya fue guardada exitosamente en el paso 1.
            System.err.println("La cita se creó, pero falló el envío del correo de confirmación. Causa: " + e.getMessage());
        }

        // 3. Devolver la respuesta exitosa al frontend con el DTO.
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCitaDTO);
    }

    private UserDTO obtenerDatosDeUsuario(Long usuarioId, String token) {
        // 💡 CAMBIO CLAVE: Usamos 'localhost' en lugar de 'authentication-service'
        // Esto es para que funcione en un entorno de desarrollo local sin contenedores.
        String url = "http://localhost:8082/auth/internal/user/" + usuarioId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<UserDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserDTO.class);
            return response.getBody();
        } catch (Exception e) {
            // Este log ahora será mucho más descriptivo si el error persiste.
            System.err.println("Error al comunicarse con authentication-service en " + url + ". Causa: " + e.getMessage());
            return null;
        }
    }


    // --- Resto de tus métodos (GET, PUT, DELETE) sin cambios ---
    @GetMapping
    public List<Cita> findAll() { return citaService.findAllCitas(); }
    // ... etc ...
}