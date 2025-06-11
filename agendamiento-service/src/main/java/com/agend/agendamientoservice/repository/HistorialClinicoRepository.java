package com.agend.agendamientoservice.repository;

import com.agend.agendamientoservice.model.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, Long> {
    List<HistorialClinico> findByMascotaId(Long mascotaId);
}