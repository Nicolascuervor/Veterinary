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
import java.util.*;

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

    public List<LocalDate> obtenerProximosDiasDisponibles(Long veterinarioId, int numDiasFuturos) {
        // 1. Obtenemos todos los días de la semana en los que el veterinario tiene algún horario configurado como DISPONIBLE.
        List<DayOfWeek> diasHabiles = disponibilidadRepository.findDistinctDiaByVeterinarioIdAndEstado(veterinarioId, EstadoDisponibilidad.DISPONIBLE);

        if (diasHabiles.isEmpty()) {
            return Collections.emptyList(); // Si no tiene días configurados, devolvemos una lista vacía.
        }

        Set<DayOfWeek> diasHabilesSet = new HashSet<>(diasHabiles);
        List<LocalDate> fechasDisponibles = new ArrayList<>();
        LocalDate fechaInicio = LocalDate.now();

        // 2. Iteramos sobre los próximos 'numDiasFuturos' días.
        for (int i = 0; i < numDiasFuturos; i++) {
            LocalDate fechaActual = fechaInicio.plusDays(i);
            // 3. Si el día de la semana de la fecha actual está en nuestro conjunto de días hábiles, lo agregamos a la lista.
            if (diasHabilesSet.contains(fechaActual.getDayOfWeek())) {
                fechasDisponibles.add(fechaActual);
            }
        }

        return fechasDisponibles;
    }


}