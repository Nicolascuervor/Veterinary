package com.agend.agendamientoservice.service;

import com.agend.agendamientoservice.model.Propietario;
import com.agend.agendamientoservice.repository.PropietarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PropietarioService {
    @Autowired
    private PropietarioRepository propietarioRepository;

    @Transactional
    public List<Propietario> findAllPropietarios() {
        return propietarioRepository.findAll();
    }

    @Transactional
    public Optional<Propietario> findPropietarioById(Long id) {
        return propietarioRepository.findById(id);
    }

    @Transactional
    public Propietario guardar(Propietario propietario) {
        return propietarioRepository.save(propietario);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!propietarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Propietario no encontrado con ID: " + id);
        }
        propietarioRepository.deleteById(id);
    }

}