package com.perfil.perfilservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
