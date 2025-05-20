package com.agend.agendamientoservice.model;

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

    private String fecha;

    @Column(name = "hora_llegada", nullable = false)
    private LocalTime horaLlegada;

    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;

    @Column(length = 1)
    private String estado; // A = (Activo), I = (Inactivo)

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Veterinario veterinario;

    @ManyToOne
    @JoinColumn(name = "area_clinica_id")
    private AreaClinica areaClinica;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}