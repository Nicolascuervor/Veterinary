package com.agend.agendamientoservice.service;


import com.agend.agendamientoservice.model.AreaClinica;
import com.agend.agendamientoservice.repository.AreaClinicaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AreaClinicaService {
    @Autowired
    private AreaClinicaRepository areaclinicaRepository;

    @Transactional
    public List<AreaClinica> findAllAreaClinicas() {
        return areaclinicaRepository.findAll();
    }

    @Transactional
    public Optional<AreaClinica> findAreaClinicaById(Long id) {
        return areaclinicaRepository.findById(id);
    }

    @Transactional
    public AreaClinica guardar(AreaClinica areaclinica) {
        return areaclinicaRepository.save(areaclinica);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!areaclinicaRepository.existsById(id)) {
            throw new EntityNotFoundException("AreaClinica no encontrado con ID: " + id);
        }
        areaclinicaRepository.deleteById(id);
    }

}
