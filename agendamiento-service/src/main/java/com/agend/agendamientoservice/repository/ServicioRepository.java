package com.agend.agendamientoservice.repository;

import com.agend.agendamientoservice.model.Especialidad;
import com.agend.agendamientoservice.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findByEspecialidad(Especialidad especialidad);

}