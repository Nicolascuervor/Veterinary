package com.agend.agendamientoservice.service;

import com.agend.agendamientoservice.model.Veterinario;
import com.agend.agendamientoservice.repository.VeterinarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VeterinarioService {
    @Autowired
    private VeterinarioRepository veterinarioRepository;

    @Transactional
    public List<Veterinario> findAllVeterinarios() {
        return veterinarioRepository.findAll();
    }

    @Transactional
    public Optional<Veterinario> findVeterinarioById(Long id) {
        return veterinarioRepository.findById(id);
    }

    @Transactional
    public Veterinario guardar(Veterinario veterinario) {
        return veterinarioRepository.save(veterinario);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!veterinarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Veterinario no encontrado con ID: " + id);
        }
        veterinarioRepository.deleteById(id);
    }

}
