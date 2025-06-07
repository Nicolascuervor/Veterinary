package com.agend.agendamientoservice.service;


import com.agend.agendamientoservice.model.Servicio;
import com.agend.agendamientoservice.repository.ServicioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioService {
    @Autowired
    private ServicioRepository ServicioRepository;

    @Transactional
    public List<Servicio> findAllServicios() {
        return ServicioRepository.findAll();
    }

    @Transactional
    public Optional<Servicio> findServicioById(Long id) {
        return ServicioRepository.findById(id);
    }

    @Transactional
    public Servicio guardar(Servicio Servicio) {
        return ServicioRepository.save(Servicio);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!ServicioRepository.existsById(id)) {
            throw new EntityNotFoundException("Servicio no encontrado con ID: " + id);
        }
        ServicioRepository.deleteById(id);
    }

}