package com.agend.agendamientoservice.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
@Entity
@Table(name = "agenda_veterinario")
@Getter
@Setter
public class AgendaVeterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAgenda estado = EstadoAgenda.ACTIVO;

    @ManyToOne(optional = false)
    @JoinColumn(name = "veterinario_id")
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
        ACTIVO,  // Bloque disponible y visible
        OCUPADO, // Cita o evento asignado
        CANCELADO,
        AUSENTE, // (opcional)
        VACACIONES // (opcional)
    }
}
