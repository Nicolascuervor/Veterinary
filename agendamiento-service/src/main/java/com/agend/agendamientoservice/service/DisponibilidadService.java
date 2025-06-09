package com.agend.agendamientoservice.service;


import com.agend.agendamientoservice.DTOs.DisponibilidadHorariaDTO;
import com.agend.agendamientoservice.controller.DisponibilidadHoraria;

import com.agend.agendamientoservice.model.EstadoDisponibilidad;
import com.agend.agendamientoservice.repository.CitaRepository;
import com.agend.agendamientoservice.repository.DisponibilidadHorariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisponibilidadService {

    private final DisponibilidadHorariaRepository disponibilidadRepository;
    private final CitaRepository citaRepository;

    public List<DisponibilidadHorariaDTO> obtenerDisponibilidad(Long veterinarioId, LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();

        List<DisponibilidadHoraria> disponibles = disponibilidadRepository
                .findByVeterinarioIdAndDiaAndEstado(veterinarioId, dia, EstadoDisponibilidad.DISPONIBLE);

        return disponibles.stream()
                .map(DisponibilidadHorariaDTO::fromEntity)
                .toList();
    }


}