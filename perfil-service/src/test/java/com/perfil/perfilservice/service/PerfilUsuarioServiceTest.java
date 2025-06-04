package com.perfil.perfilservice.service;

import com.perfil.perfilservice.dto.PerfilUsuarioCreateDTO;
import com.perfil.perfilservice.dto.PerfilUsuarioDTO;
import com.perfil.perfilservice.model.PerfilUsuario;
import com.perfil.perfilservice.repository.ContactoEmergenciaRepository;
import com.perfil.perfilservice.repository.PerfilUsuarioRepository;
import com.perfil.perfilservice.repository.PreferenciaPrivacidadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PerfilUsuarioServiceTest {

    @Mock
    private PerfilUsuarioRepository perfilUsuarioRepository;

    @Mock
    private ContactoEmergenciaRepository contactoEmergenciaRepository;

    @Mock
    private PreferenciaPrivacidadRepository preferenciaPrivacidadRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PerfilUsuarioService perfilUsuarioService;

    @Test
    void crearPerfil_DeberiaCrearPerfilExitosamente() {
        // Arrange
        PerfilUsuarioCreateDTO createDTO = new PerfilUsuarioCreateDTO();
        createDTO.setUserId(1L);
        createDTO.setNombre("Juan");
        createDTO.setApellido("Pérez");
        createDTO.setEmail("juan.perez@email.com");
        createDTO.setTelefono("1234567890");
        createDTO.setFechaNacimiento(LocalDate.of(1990, 1, 1));

        PerfilUsuario perfilGuardado = new PerfilUsuario();
        perfilGuardado.setId(1L);
        perfilGuardado.setUserId(1L);
        perfilGuardado.setNombre("Juan");
        perfilGuardado.setApellido("Pérez");

        when(perfilUsuarioRepository.existsByUserId(1L)).thenReturn(false);
        when(perfilUsuarioRepository.existsByEmail("juan.perez@email.com")).thenReturn(false);
        when(perfilUsuarioRepository.save(any(PerfilUsuario.class))).thenReturn(perfilGuardado);

        // Act
        PerfilUsuarioDTO resultado = perfilUsuarioService.crearPerfil(createDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        assertEquals("Pérez", resultado.getApellido());
        verify(perfilUsuarioRepository).save(any(PerfilUsuario.class));
        verify(emailService).enviarEmailBienvenidaPerfil(any(PerfilUsuario.class));
    }

    @Test
    void crearPerfil_DeberiaLanzarExcepcionSiUsuarioYaExiste() {
        // Arrange
        PerfilUsuarioCreateDTO createDTO = new PerfilUsuarioCreateDTO();
        createDTO.setUserId(1L);

        when(perfilUsuarioRepository.existsByUserId(1L)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> perfilUsuarioService.crearPerfil(createDTO)
        );

        assertEquals("Ya existe un perfil para este usuario", exception.getMessage());
        verify(perfilUsuarioRepository, never()).save(any());
    }

    @Test
    void crearPerfil_DeberiaLanzarExcepcionSiEmailYaExiste() {
        // Arrange
        PerfilUsuarioCreateDTO createDTO = new PerfilUsuarioCreateDTO();
        createDTO.setUserId(1L);
        createDTO.setEmail("juan.perez@email.com");

        when(perfilUsuarioRepository.existsByUserId(1L)).thenReturn(false);
        when(perfilUsuarioRepository.existsByEmail("juan.perez@email.com")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> perfilUsuarioService.crearPerfil(createDTO)
        );

        assertEquals("El email ya está en uso", exception.getMessage());
    }
}