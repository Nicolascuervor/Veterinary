package com.agend.agendamientoservice.service;

import com.agend.agendamientoservice.model.Cita;
import com.agend.agendamientoservice.repository.CitaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CitaService {
    @Autowired
    private CitaRepository citaRepository;

    @Transactional
    public List<Cita> findAllCitas() {
        return citaRepository.findAll();
    }

    @Transactional
    public Optional<Cita> findCitaById(Long id) {
        return citaRepository.findById(id);
    }

    @Transactional
    public Cita guardar(Cita cita) {
        return citaRepository.save(cita);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!citaRepository.existsById(id)) {
            throw new EntityNotFoundException("Cita no encontrado con ID: " + id);
        }
        citaRepository.deleteById(id);
    }

}