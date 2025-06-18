package com.agend.agendamientoservice.service;

import com.agend.agendamientoservice.DTOs.MascotaResponseDTO;
import com.agend.agendamientoservice.model.Mascota;
import com.agend.agendamientoservice.repository.MascotaRepository;
import com.agend.agendamientoservice.repository.PropietarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MascotaService {
    @Autowired
    private MascotaRepository mascotaRepository;


    @Transactional
    public List<MascotaResponseDTO> findAllMascotas() {
        return mascotaRepository.findAll()
                .stream()
                .map(MascotaResponseDTO::new) // Convierte cada Mascota a MascotaResponseDTO
                .collect(Collectors.toList());
    }



    @Transactional
    public Optional<Mascota> findMascotaById(Long id) {
        return mascotaRepository.findById(id);
    }

    // MÉTODO MODIFICADO
    @Transactional
    public MascotaResponseDTO findById(Long id) {
        Mascota mascota = mascotaRepository.findByIdWithPropietario(id).orElse(null);
        if (mascota == null) {
            return null;
        }
        return new MascotaResponseDTO(mascota); // Convierte la Mascota encontrada a DTO
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
    public List<MascotaResponseDTO> findByPropietarioId(Long propietarioId) {
        return mascotaRepository.findByPropietarioId(propietarioId)
                .stream()
                .map(MascotaResponseDTO::new) // Mapea cada entidad Mascota al DTO
                .collect(Collectors.toList());
    }

}