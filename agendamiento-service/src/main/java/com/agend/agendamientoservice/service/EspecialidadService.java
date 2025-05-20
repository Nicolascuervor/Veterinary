package com.agend.agendamientoservice.service;


import com.agend.agendamientoservice.model.Especialidad;
import com.agend.agendamientoservice.repository.EspecialidadRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EspecialidadService {
    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Transactional
    public List<Especialidad> findAllEspecialidads() {
        return especialidadRepository.findAll();
    }

    @Transactional
    public Optional<Especialidad> findEspecialidadById(Long id) {
        return especialidadRepository.findById(id);
    }

    @Transactional
    public Especialidad guardar(Especialidad especialidad) {
        return especialidadRepository.save(especialidad);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!especialidadRepository.existsById(id)) {
            throw new EntityNotFoundException("Especialidad no encontrado con ID: " + id);
        }
        especialidadRepository.deleteById(id);
    }

}
