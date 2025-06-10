package com.agend.agendamientoservice.repository;

import com.agend.agendamientoservice.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByPropietarioId(Long propietarioId);
}
