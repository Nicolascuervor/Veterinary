package com.agend.agendamientoservice.repository;

import com.agend.agendamientoservice.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Busca todas las citas de una mascota específica, ordenadas por fecha descendente.
     * Esencial para el módulo de historial clínico.
     * @param mascotaId El ID de la mascota.
     * @return Una lista de citas ordenadas.
     */
    List<Cita> findByMascotaIdOrderByFechaDesc(Long mascotaId);

    /**
     * Busca todas las citas para un veterinario en una fecha específica.
     * Útil para ver la agenda del día de un veterinario.
     * @param veterinarioId El ID del veterinario.
     * @param fecha La fecha a consultar.
     * @return Una lista de citas para ese día y veterinario.
     */
    List<Cita> findByVeterinarioIdAndFecha(Long veterinarioId, LocalDate fecha);

}