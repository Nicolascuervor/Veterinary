package com.agend.agendamientoservice.repository;

import com.agend.agendamientoservice.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ServicioRepository extends JpaRepository<Servicio, Long> { }