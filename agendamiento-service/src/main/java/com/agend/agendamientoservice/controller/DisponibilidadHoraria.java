package com.agend.agendamientoservice.controller;

import com.agend.agendamientoservice.model.Veterinario;
import com.agend.agendamientoservice.model.EstadoDisponibilidad;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;


@Entity
@Getter
@Setter
public class DisponibilidadHoraria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private DayOfWeek dia;

    private LocalTime horaInicio;
    private LocalTime horaFin;

    @ManyToOne
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Veterinario veterinario;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoDisponibilidad estado = EstadoDisponibilidad.DISPONIBLE;
}
