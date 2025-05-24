package com.gateway.authenticationservice.Controller;


import com.gateway.authenticationservice.ConfigSecurity.JwtService;
import com.gateway.authenticationservice.model.User;
import com.gateway.authenticationservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(request.getUsername(), request.getPassword(), String.valueOf(request.getRole()));
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
        return ResponseEntity.ok(new AuthResponse(token, String.valueOf(user.getRole())));
    }

    @GetMapping("/admin/test")
    public ResponseEntity<String> adminTest() {
        return ResponseEntity.ok("Admin access granted");
    }
}
