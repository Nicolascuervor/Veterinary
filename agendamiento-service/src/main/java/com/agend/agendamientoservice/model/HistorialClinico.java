package com.agend.agendamientoservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "historial_clinico")
public class HistorialClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "veterinario")
    private String veterinario;

    @Column(name = "tratamiento")
    private String tratamiento;
}