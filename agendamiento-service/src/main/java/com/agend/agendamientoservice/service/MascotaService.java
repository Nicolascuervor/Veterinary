package com.agend.agendamientoservice.service;

import com.agend.agendamientoservice.model.Mascota;
import com.agend.agendamientoservice.repository.MascotaRepository;
import com.agend.agendamientoservice.repository.PropietarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MascotaService {
    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    @Transactional
    public List<Mascota> findAllMascotas() {
        return mascotaRepository.findAll();
    }

    @Transactional
    public Optional<Mascota> findMascotaById(Long id) {
        return mascotaRepository.findById(id);
    }

    @Transactional
    public Mascota guardar(Mascota mascota) {
        return mascotaRepository.save(mascota);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!mascotaRepository.existsById(id)) {
            throw new EntityNotFoundException("Mascota no encontrado con ID: " + id);
        }
        mascotaRepository.deleteById(id);
    }

    @Transactional
    public List<Mascota> findByPropietarioId(Long propietarioId) {
        return mascotaRepository.findByPropietarioId(propietarioId);
    }

}