package com.gateway.authenticationservice.Controller;


import com.gateway.authenticationservice.ConfigSecurity.JwtService;
import com.gateway.authenticationservice.Emails.EmailClient;
import com.gateway.authenticationservice.model.PasswordResetToken;
import com.gateway.authenticationservice.model.User;
import com.gateway.authenticationservice.repository.PasswordResetTokenRepository;
import com.gateway.authenticationservice.repository.UserRepository;
import com.gateway.authenticationservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(
                request.getUsername(),
                request.getPassword(),
                request.getNombre(),
                request.getApellido(),
                request.getTelefono(),
                request.getDireccion(),
                String.valueOf(request.getRole()));
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
        String nombre = user.getNombre();

        return ResponseEntity.ok(new AuthResponse(token, String.valueOf(user.getRole()), request.getUsername(), nombre));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        Optional<User> userOpt = userRepository.findByUsername(email); // si username = correo
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Correo no registrado");
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();

        // Guardar el token en memoria o base de datos con expiración
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiration(LocalDateTime.now().plusMinutes(30));
        passwordResetTokenRepository.save(resetToken);

        // Crear enlace para el correo
        String resetLink = "http://localhost:5500/veterinaria-frontend/pages/reset-password.html?token=" + token;

        // Enviar correo
        Map<String, Object> model = Map.of(
                "nombre", user.getNombre(),
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

        passwordResetTokenRepository.delete(resetToken); // eliminar token tras uso

        // Enviar correo al usuario
        emailClient.enviarEmail(
                new String[]{user.getUsername()},
                "Contraseña restablecida correctamente",
                "Hola " + user.getNombre() + "\n" +
                        "Le informamos que su contraseña ha sido restaurada correctamente. Ya puede acceder a su cuenta utilizando la nueva contraseña establecida..\n" +
                        "\n" +
                        "Si usted no solicitó este cambio, por favor contáctenos de inmediato para asegurar la protección de su cuenta..\n" +
                        "\n" +
                        "Gracias por confiar en nosotros.\n"
        );

        return ResponseEntity.ok("Contraseña actualizada correctamente");

    }



    @GetMapping("/admin/test")
    public ResponseEntity<String> adminTest() {
        return ResponseEntity.ok("Admin access granted");
    }
}
