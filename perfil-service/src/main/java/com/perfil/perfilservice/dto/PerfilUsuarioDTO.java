package com.perfil.perfilservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUsuarioDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;

    @Email(message = "El formato del email no es válido")
    private String email;

    @Pattern(regexp = "\\d{10}", message = "El teléfono debe tener 10 dígitos")
    private String telefono;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    private String genero;

    private String ocupacion;

    private String cedula;

    private String fotoPerfilUrl;

    @Size(max = 500, message = "La biografía no puede exceder 500 caracteres")
    private String biografia;

    private String estadoPerfil;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaUltimaActualizacion;

    private List<ContactoEmergenciaDTO> contactosEmergencia;

    private List<PreferenciaPrivacidadDTO> preferenciasPrivacidad;

    // Campo calculado
    private String nombreCompleto;

    private Boolean perfilCompleto;
}

