package com.agend.agendamientoservice.repository;

import com.agend.agendamientoservice.model.AgendaVeterinario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AgendaVeterinarioRepository extends JpaRepository<AgendaVeterinario, Long> {
    List<AgendaVeterinario> findByVeterinarioIdAndFecha(Long veterinarioId, LocalDate fecha);
    List<AgendaVeterinario> findByVeterinarioIdAndFechaAndEstado(Long veterinarioId, LocalDate fecha, AgendaVeterinario.EstadoAgenda estado);
}