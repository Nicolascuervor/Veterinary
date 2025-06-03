package com.perfil.perfilservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
