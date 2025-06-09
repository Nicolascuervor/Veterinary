package com.agend.agendamientoservice.repository;


import com.agend.agendamientoservice.controller.DisponibilidadHoraria;
import com.agend.agendamientoservice.model.EstadoDisponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface DisponibilidadHorariaRepository extends JpaRepository<DisponibilidadHoraria, Long> {
    List<DisponibilidadHoraria> findByVeterinarioIdAndDiaAndEstado(
            Long veterinarioId, DayOfWeek dia, EstadoDisponibilidad estado
    );

    Optional<DisponibilidadHoraria> findByVeterinarioIdAndDiaAndHoraInicio(
            Long veterinarioId, DayOfWeek dia, LocalTime horaInicio
    );


}