package com.agend.agendamientoservice.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "prescripcion_medica")
public class PrescripcionMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encuentro_id", nullable = false)
    private EncuentroClinico encuentro;

    @Column(name = "medicamento", nullable = false, length = 200)
    private String medicamento;

    @Column(name = "dosis", length = 100)
    private String dosis;

    @Column(name = "frecuencia", length = 100)
    private String frecuencia;

    @Column(name = "duracion", length = 100)
    private String duracion; // Ej: "7 días", "30 días"

    @Column(name = "instrucciones", columnDefinition = "TEXT")
    private String instrucciones;
}