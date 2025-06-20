package com.agend.agendamientoservice.repository;

import com.agend.agendamientoservice.model.Especialidad;
import com.agend.agendamientoservice.model.Propietario;
import com.agend.agendamientoservice.model.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {
    List<Veterinario> findByEspecialidad(Especialidad especialidad);




}
