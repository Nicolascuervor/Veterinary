package com.agend.agendamientoservice.service;


import com.agend.agendamientoservice.model.DisponibilidadVeterinario;
import com.agend.agendamientoservice.repository.DisponibilidadVeterinarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DisponibilidadVeterinarioService {
    @Autowired
    private DisponibilidadVeterinarioRepository disponibilidadveterinarioRepository;

    @Transactional
    public List<DisponibilidadVeterinario> findAllDisponibilidadVeterinarios() {
        return disponibilidadveterinarioRepository.findAll();
    }

    @Transactional
    public Optional<DisponibilidadVeterinario> findDisponibilidadVeterinarioById(Long id) {
        return disponibilidadveterinarioRepository.findById(id);
    }

    @Transactional
    public DisponibilidadVeterinario guardar(DisponibilidadVeterinario disponibilidadveterinario) {
        return disponibilidadveterinarioRepository.save(disponibilidadveterinario);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!disponibilidadveterinarioRepository.existsById(id)) {
            throw new EntityNotFoundException("DisponibilidadVeterinario no encontrado con ID: " + id);
        }
        disponibilidadveterinarioRepository.deleteById(id);
    }

}