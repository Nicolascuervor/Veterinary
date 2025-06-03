package com.perfil.perfilservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
