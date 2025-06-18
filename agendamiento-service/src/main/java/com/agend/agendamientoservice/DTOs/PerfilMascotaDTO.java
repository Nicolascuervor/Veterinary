package com.agend.agendamientoservice.DTOs;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class PerfilMascotaDTO {
    private Long id;
    private String nombre;
    private String especie;
    private String raza;
    private LocalDate fechaNacimiento;
    private String sexo;
    private String fotoUrl;
    private String propietarioNombre;
}