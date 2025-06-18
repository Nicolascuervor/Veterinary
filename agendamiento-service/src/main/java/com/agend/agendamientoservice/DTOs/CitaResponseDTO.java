package com.agend.agendamientoservice.DTOs;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
@Data
public class CitaResponseDTO {
    private Long id;
    private String motivo;
    private LocalDate fecha;
    private LocalTime hora;
    private String estadoCita;
    private String mascotaNombre;
    private String veterinarioNombre;
    private String servicioNombre;
}