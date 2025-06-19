package com.agend.agendamientoservice.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class CitaHistorialDTO {
    private Long id;
    private LocalDateTime fecha;
    private String motivoServicio;
    private String estadoCita;
    private String veterinarioNombre;
    private EncuentroClinicoDTO encuentro;
}