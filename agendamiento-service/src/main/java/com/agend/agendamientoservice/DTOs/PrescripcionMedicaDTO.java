package com.agend.agendamientoservice.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrescripcionMedicaDTO {
    private String medicamento;
    private String dosis;
    private String frecuencia;
    private String duracion;
    private String instrucciones;
}