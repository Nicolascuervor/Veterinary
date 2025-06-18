package com.agend.agendamientoservice.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HistorialCompletoDTO {
    private PerfilMascotaDTO perfilMascota;
    private List<CitaHistorialDTO> citas;
    private List<VacunacionDTO> vacunaciones;
}