package com.agend.agendamientoservice.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SignosVitalesDTO {
    private BigDecimal pesoKg;
    private BigDecimal temperaturaCelsius;
    private Integer frecuenciaCardiaca;
    private Integer frecuenciaRespiratoria;
}
