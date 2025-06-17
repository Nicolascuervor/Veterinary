package com.agend.agendamientoservice.repository;

import com.agend.agendamientoservice.model.Propietario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropietarioRepository extends JpaRepository<Propietario, Long> {
    Optional<Propietario> findByUsuarioId(Long usuarioId);






}
