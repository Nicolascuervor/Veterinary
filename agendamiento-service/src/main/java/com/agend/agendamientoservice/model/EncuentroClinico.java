package com.agend.agendamientoservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "encuentro_clinico")
public class EncuentroClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Vincula directamente el encuentro con la cita agendada.
    @OneToOne
    @JoinColumn(name = "cita_id", referencedColumnName = "id")
    private Cita cita;

    @Column(name = "fecha_encuentro", nullable = false)
    private LocalDateTime fechaEncuentro;

    @Column(name = "motivo_consulta", length = 500)
    private String motivoConsulta;

    // Notas del veterinario sobre el examen físico y la conversación con el propietario.
    @Column(name = "anamnesis", columnDefinition = "TEXT")
    private String anamnesis;

    @Column(name = "diagnostico_principal", columnDefinition = "TEXT")
    private String diagnosticoPrincipal;

    @Column(name = "tratamiento_sugerido", columnDefinition = "TEXT")
    private String tratamientoSugerido;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // Relaciones con los nuevos modelos de datos estructurados.
    @OneToOne(mappedBy = "encuentro", cascade = CascadeType.ALL)
    private SignosVitales signosVitales;

    @OneToMany(mappedBy = "encuentro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PrescripcionMedica> prescripciones;
}