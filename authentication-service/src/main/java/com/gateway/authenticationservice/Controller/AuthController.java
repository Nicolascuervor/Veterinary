package com.gateway.authenticationservice.Controller;

import com.gateway.authenticationservice.ConfigSecurity.JwtService;
import com.gateway.authenticationservice.DTOs.UserDTO;
import com.gateway.authenticationservice.Emails.EmailClient;
import com.gateway.authenticationservice.model.*;
import com.gateway.authenticationservice.repository.PasswordResetTokenRepository;
import com.gateway.authenticationservice.repository.UserRepository;
import com.gateway.authenticationservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;


    private final EmailClient emailClient;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    @Value("${app.gateway.url}") // <-- Inyecta el valor desde application.yml
    private String gatewayUrl;

    // --- AÑADE ESTAS LÍNEAS ---
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostConstruct
    public void init() {
        logger.info("✅ AuthController inicializado. El valor inyectado para 'gatewayUrl' es: '{}'", gatewayUrl);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        User user = this.registerUserYPropietario(request);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(user);
        PropietarioResponse propietario = obtenerDatosPropietario(user.getId(), token);

        String nombre = (propietario != null) ? propietario.getNombre() : "usuario";
        Long propietarioId = (propietario != null) ? propietario.getId() : null;

        return ResponseEntity.ok(new AuthResponse(token, String.valueOf(user.getRole()), request.getUsername(), nombre, propietarioId));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<User> userOpt = userRepository.findByUsername(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Correo no registrado");
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiration(LocalDateTime.now().plusMinutes(30));
        passwordResetTokenRepository.save(resetToken);

        String resetLink = "http://localhost:5500/veterinaria-frontend/pages/reset-password.html?token=" + token;
        String jwtToken = jwtService.generateToken(user);

        PropietarioResponse propietario = obtenerDatosPropietario(user.getId(), jwtToken);
        String nombre = (propietario != null) ? propietario.getNombre() : "usuario";

        Map<String, Object> model = Map.of(
                "nombre", nombre,
                "link", resetLink
        );

        emailClient.sendEmailTemplate(
                new String[]{email},
                "Restablece tu contraseña",
                "email-reset-password",
                model
        );

        return ResponseEntity.ok("Enlace enviado");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");

        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Token inválido");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.getExpiration().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE).body("El token ha expirado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);

        String jwtToken = jwtService.generateToken(user);
        PropietarioResponse propietario = obtenerDatosPropietario(user.getId(), jwtToken);
        String nombre = (propietario != null) ? propietario.getNombre() : "usuario";

        emailClient.enviarEmail(
                new String[]{user.getUsername()},
                "Contraseña restablecida correctamente",
                "Hola " + nombre + "\n" +
                        "Le informamos que su contraseña ha sido restaurada correctamente. Ya puede acceder a su cuenta utilizando la nueva contraseña establecida.\n" +
                        "\n" +
                        "Si usted no solicitó este cambio, por favor contáctenos de inmediato para asegurar la protección de su cuenta.\n" +
                        "Gracias por confiar en nosotros."
        );

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    public User registerUserYPropietario(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(true);
        user = userRepository.save(user);

        PropietarioRequest propietario = new PropietarioRequest();
        propietario.nombre = request.getNombre();
        propietario.apellido = request.getApellido();
        propietario.direccion = request.getDireccion();
        propietario.telefono = request.getTelefono();
        propietario.cedula = request.getCedula();
        propietario.usuarioId = user.getId();


        try {
            String token = jwtService.generateToken(user);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<PropietarioRequest> entity = new HttpEntity<>(propietario, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    gatewayUrl + "/propietarios/registrar", // <-- ¡CORREGIDO!
                    HttpMethod.POST,
                    entity,
                    String.class
            );



            String nombre = (propietario != null) ? propietario.getNombre() : "usuario";


            // Enviar correo al usuario
            emailClient.enviarEmail(
                    new String[]{user.getUsername()},
                    "¡Bienvenido/a a la Veterinaria de la CUE Alexander von Humboldt!",
                    "Hola " + nombre + "\n" +
                            "Nos alegra contar contigo como parte de nuestra comunidad. Desde ahora podrás acceder fácilmente a nuestros servicios, agendar citas, consultar el historial médico de tus mascotas, recibir recordatorios importantes y mucho más, todo desde un mismo lugar.\n" +
                            "\n" +
                            "Estamos comprometidos con el bienestar animal y con ofrecerte una atención de calidad, respaldada por profesionales y estudiantes capacitados de nuestra universidad.\n" +
                            "\n" +
                            "Si tienes alguna duda o necesitas asistencia, no dudes en contactarnos.\n" +
                            "¡Esperamos poder ayudarte a cuidar de tus compañeros peludos!"
            );

            System.out.println("✅ Propietario creado: " + response.getStatusCode());

        } catch (Exception e) {
            System.out.println("❌ Error creando propietario: " + e.getMessage());
            e.printStackTrace();
            userRepository.delete(user);
            throw new RuntimeException("Fallo al crear propietario");
        }

        return user;
    }



    private PropietarioResponse obtenerDatosPropietario(Long usuarioId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<PropietarioResponse> response = restTemplate.exchange(
                    gatewayUrl + "/propietarios/usuario/" + usuarioId, // <-- ¡CORREGIDO!
                    HttpMethod.GET,
                    entity,
                    PropietarioResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            System.out.println("❌ No se pudo obtener el propietario para usuarioId: " + usuarioId);
            return null;
        }
    }

    /**
     * Endpoint interno para obtener datos básicos de un usuario por su ID.
     * Protegido por la autenticación del gateway.
     * @param id El ID del usuario a buscar.
     * @return Un DTO con la información del usuario.
     */
    @GetMapping("/internal/user/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    // Mapeamos la entidad User a nuestro UserDTO
                    UserDTO userDTO = new UserDTO(
                            user.getId(),
                            user.getUsername(), // El username es el email
                            user.getRole().name()
                    );
                    return ResponseEntity.ok(userDTO);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/test")
    public ResponseEntity<String> adminTest() {
        return ResponseEntity.ok("Admin access granted");
    }
}
