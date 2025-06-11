package com.agend.agendamientoservice.controller;

import com.agend.agendamientoservice.DTOs.CitaRequest;
import com.agend.agendamientoservice.DTOs.UserDTO;
import com.agend.agendamientoservice.model.Cita;
import com.agend.agendamientoservice.model.Mascota;
import com.agend.agendamientoservice.model.Propietario;
import com.agend.agendamientoservice.model.Veterinario;
import com.agend.agendamientoservice.service.CitaService;
import com.agend.agendamientoservice.Emails.EmailClient; // Asegúrate que esta clase exista y esté configurada.


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.List;


@RestController
@RequestMapping("/cita")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;
    private final EmailClient emailClient;
    private final RestTemplate restTemplate;

    @PostMapping("/registrar")
    public ResponseEntity<Cita> guardarCita(@RequestBody CitaRequest request, HttpServletRequest httpRequest) {
        // 1. Guardar la cita
        Cita nuevaCita = citaService.convertirYGuardar(request);

        // 2. Extraer la información necesaria de la cita guardada
        Propietario propietario = nuevaCita.getPropietario();
        if (propietario == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "La cita se guardó sin un propietario asociado.");
        }
        Long usuarioId = propietario.getUsuarioId();
        Mascota mascota = nuevaCita.getMascota();
        Veterinario veterinario = nuevaCita.getVeterinario();

        // 3. Obtener el token de la petición actual para autenticar la llamada interna
        final String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta el token de autorización para la comunicación interna.");
        }

        // 4. Llamar al authentication-service para obtener los datos del usuario (su email)
        UserDTO usuario = obtenerDatosDeUsuario(usuarioId, authHeader);
        if (usuario == null || usuario.getEmail() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se pudieron obtener los datos del usuario desde el servicio de autenticación.");
        }

        // 5. Preparar y enviar el correo de confirmación
        try {
            String nombrePropietario = propietario.getNombre();
            String nombreMascota = (mascota != null) ? mascota.getNombre() : "Tu mascota";
            String nombreVeterinario = (veterinario != null) ? veterinario.getNombre() : "Asignado";

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy 'a las' HH:mm 'horas'");
            String fechaFormateada = nuevaCita.getFecha().atTime(nuevaCita.getHora()).format(formatter);

            String asunto = "Confirmación de Cita para " + nombreMascota;
            String mensaje = String.format(
                    "Hola %s,\n\n" +
                            "Te confirmamos que tu cita para %s ha sido agendada con éxito.\n\n" +
                            "Detalles de la cita:\n" +
                            "  - Fecha y Hora: %s\n" +
                            "  - Veterinario: Dr. %s\n" +
                            "  - Motivo: %s\n\n" +
                            "Gracias por confiar en VetCare.",
                    nombrePropietario, nombreMascota, fechaFormateada, nombreVeterinario, nuevaCita.getMotivo()
            );

            emailClient.enviarEmail(new String[]{usuario.getEmail()}, asunto, mensaje);

        } catch (Exception e) {
            // Si el envío del correo falla, no cancelamos la cita, solo registramos el error.
            System.err.println("La cita se creó, pero falló el envío del correo de confirmación. Causa: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
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