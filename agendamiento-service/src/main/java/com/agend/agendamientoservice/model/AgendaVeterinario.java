package com.agend.agendamientoservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "agenda_veterinario")
public class AgendaVeterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    // Estados: DISPONIBLE, OCUPADO, CANCELADO
    @Enumerated(EnumType.STRING)
    private EstadoAgenda estado = EstadoAgenda.ACTIVO;

    @ManyToOne(optional = false)
    @JoinColumn(name = "veterinario_id")
    @JsonBackReference
    private Veterinario veterinario;

    @ManyToOne
    @JoinColumn(name = "area_clinica_id")
    private AreaClinica areaClinica;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum EstadoAgenda {
        ACTIVO,
        OCUPADO,
        CANCELADO
    }
}