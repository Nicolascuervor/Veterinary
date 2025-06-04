package com.perfil.perfilservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
