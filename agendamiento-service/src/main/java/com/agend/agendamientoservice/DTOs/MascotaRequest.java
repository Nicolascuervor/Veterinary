package com.agend.agendamientoservice.DTOs;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MascotaRequest {
    public String nombre;
    public String edad;
    public String especie;
    public String raza;
    public Long propietarioId;
    public String color;
    private String sexo;
    public LocalDate fechaNacimiento;
    public Double peso;
    private String imageUrl;

}
