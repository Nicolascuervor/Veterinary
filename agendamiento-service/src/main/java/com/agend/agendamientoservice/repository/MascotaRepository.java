package com.agend.agendamientoservice.repository;

import com.agend.agendamientoservice.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByPropietarioId(Long propietarioId);

    @Query("SELECT m FROM Mascota m JOIN FETCH m.propietario WHERE m.id = :id")
    Optional<Mascota> findByIdWithPropietario(@Param("id") Long id);
}
