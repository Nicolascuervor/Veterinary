package com.agend.agendamientoservice.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class EncuentroClinicoDTO {
    private Long id;
    private LocalDateTime fechaEncuentro;
    private String motivoConsulta;
    private String anamnesis;
    private String diagnosticoPrincipal;
    private String tratamientoSugerido;
    private String observaciones;
    private SignosVitalesDTO signosVitales;
    private List<PrescripcionMedicaDTO> prescripciones;
}