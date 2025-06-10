package com.agend.agendamientoservice.DTOs;


import com.agend.agendamientoservice.controller.DisponibilidadHoraria;
import lombok.Data;

import java.time.LocalTime;

@Data
public class DisponibilidadHorariaDTO {
    private Long id;
    private String horaInicio;
    private String horaFin;
    private String estado;

    public static DisponibilidadHorariaDTO fromEntity(DisponibilidadHoraria entidad) {
        DisponibilidadHorariaDTO dto = new DisponibilidadHorariaDTO();
        dto.setId(entidad.getId());
        dto.setHoraInicio(entidad.getHoraInicio().toString());
        dto.setHoraFin(entidad.getHoraFin().toString());
        dto.setEstado(entidad.getEstado().name());
        return dto;
    }
}