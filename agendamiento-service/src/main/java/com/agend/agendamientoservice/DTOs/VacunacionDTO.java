package com.agend.agendamientoservice.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class VacunacionDTO {
    private Long id;
    private String nombreVacuna;
    private String lote;
    private LocalDate fechaAplicacion;
    private LocalDate fechaProximaDosis;
    private String veterinarioNombre;
}