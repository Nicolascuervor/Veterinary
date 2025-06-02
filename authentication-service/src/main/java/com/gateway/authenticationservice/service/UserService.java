package com.gateway.authenticationservice.service;


import com.gateway.authenticationservice.Emails.EmailClient;
import com.gateway.authenticationservice.model.Role;
import com.gateway.authenticationservice.model.User;
import com.gateway.authenticationservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    @Autowired
    private EmailClient emailClient;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User registerUser(String username, String password,String nombre, String apellido,String telefono,String direccion, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNombre(nombre);
        user.setApellido(apellido);
        user.setTelefono(telefono);
        user.setDireccion(direccion);
        user.setRole(Role.valueOf(role.toUpperCase()));


        // Enviar correo al usuario
        emailClient.enviarEmail(
                new String[]{user.getUsername()},
                "¡Bienvenido/a a la Veterinaria de la CUE Alexander von Humboldt!",
                "Hola " + user.getNombre() + "\n" +
                        "Nos alegra contar contigo como parte de nuestra comunidad. Desde ahora podrás acceder fácilmente a nuestros servicios, agendar citas, consultar el historial médico de tus mascotas, recibir recordatorios importantes y mucho más, todo desde un mismo lugar.\n" +
                        "\n" +
                        "Estamos comprometidos con el bienestar animal y con ofrecerte una atención de calidad, respaldada por profesionales y estudiantes capacitados de nuestra universidad.\n" +
                        "\n" +
                        "Si tienes alguna duda o necesitas asistencia, no dudes en contactarnos.\n" +
                        "¡Esperamos poder ayudarte a cuidar de tus compañeros peludos!"
        );


        return userRepository.save(user);
    }
}


