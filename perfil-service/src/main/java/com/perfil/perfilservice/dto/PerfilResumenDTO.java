package com.perfil.perfilservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
