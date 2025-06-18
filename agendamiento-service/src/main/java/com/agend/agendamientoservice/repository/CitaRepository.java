package com.agend.agendamientoservice.repository;

import com.agend.agendamientoservice.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByVeterinarioIdAndFecha(Long veterinarioId, LocalDate fecha);

    List<Cita> findByMascotaIdOrderByFechaDesc(Long mascotaId);


}
