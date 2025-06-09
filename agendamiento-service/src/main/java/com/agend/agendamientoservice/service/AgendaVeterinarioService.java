package com.agend.agendamientoservice.service;
import com.agend.agendamientoservice.model.AgendaVeterinario;
import com.agend.agendamientoservice.repository.AgendaVeterinarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AgendaVeterinarioService {
    @Autowired
    private AgendaVeterinarioRepository agendaveterinarioRepository;

    public List<AgendaVeterinario> findDisponibles(Long vetId, LocalDate fecha) {
        return agendaveterinarioRepository.findByVeterinarioIdAndFechaAndEstado(vetId, fecha, AgendaVeterinario.EstadoAgenda.ACTIVO);
    }

    @Transactional
    public List<AgendaVeterinario> findAllAgendaVeterinarios() {
        return agendaveterinarioRepository.findAll();
    }

    @Transactional
    public Optional<AgendaVeterinario> findAgendaVeterinarioById(Long id) {
        return agendaveterinarioRepository.findById(id);
    }

    @Transactional
    public AgendaVeterinario guardar(AgendaVeterinario agendaveterinario) {
        return agendaveterinarioRepository.save(agendaveterinario);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!agendaveterinarioRepository.existsById(id)) {
            throw new EntityNotFoundException("AgendaVeterinario no encontrado con ID: " + id);
        }
        agendaveterinarioRepository.deleteById(id);
    }

}