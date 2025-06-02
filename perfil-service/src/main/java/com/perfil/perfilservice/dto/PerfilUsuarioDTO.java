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

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactoEmergenciaDTO {

    private Long id;

    @NotBlank(message = "El nombre del contacto es obligatorio")
    private String nombre;

    @NotBlank(message = "El teléfono del contacto es obligatorio")
    @Pattern(regexp = "\\d{10}", message = "El teléfono debe tener 10 dígitos")
    private String telefono;

    @NotBlank(message = "La relación es obligatoria")
    private String relacion;

    @Email(message = "El formato del email no es válido")
    private String email;

    private Boolean esContactoPrincipal;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreferenciaPrivacidadDTO {

    private Long id;

    @NotBlank(message = "El tipo de preferencia es obligatorio")
    private String tipoPreferencia;

    @NotNull(message = "El valor de la preferencia es obligatorio")
    private Boolean valor;

    private String descripcion;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUsuarioCreateDTO {

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long userId;

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

    private String direccion;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    private String genero;

    private String ocupacion;

    private String cedula;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUsuarioUpdateDTO {

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

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
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilResumenDTO {

    private Long id;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String fotoPerfilUrl;
    private String estadoPerfil;
    private Boolean perfilCompleto;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaUltimaActualizacion;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CambioPasswordDTO {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String passwordActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    private String nuevaPassword;

    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmarPassword;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasPerfilDTO {

    private Long totalPerfiles;
    private Long perfilesActivos;
    private Long perfilesInactivos;
    private Long perfilesCompletos;
    private Long perfilesIncompletos;
    private Double porcentajeComplecion;
}