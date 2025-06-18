package com.agend.agendamientoservice.repository;

import com.agend.agendamientoservice.model.Vacunacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacunacionRepository extends JpaRepository<Vacunacion, Long> {
    List<Vacunacion> findByMascotaIdOrderByFechaAplicacionDesc(Long mascotaId);
}