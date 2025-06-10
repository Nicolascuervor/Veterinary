package com.agend.agendamientoservice.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CitaRequest {
    public String motivo;
    public String fecha; // formato: yyyy-MM-dd
    public String hora;  // formato: HH:mm:ss
    public Long mascotaId;
    public Long veterinarioId;
}