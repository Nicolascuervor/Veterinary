package com.agend.agendamientoservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "signos_vitales")
public class SignosVitales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "encuentro_id", nullable = false)
    private EncuentroClinico encuentro;

    @Column(name = "peso_kg", precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "temperatura_celsius", precision = 4, scale = 1)
    private BigDecimal temperaturaCelsius;

    @Column(name = "frecuencia_cardiaca")
    private Integer frecuenciaCardiaca; // Pulsaciones por minuto

    @Column(name = "frecuencia_respiratoria")
    private Integer frecuenciaRespiratoria; // Respiraciones por minuto
}