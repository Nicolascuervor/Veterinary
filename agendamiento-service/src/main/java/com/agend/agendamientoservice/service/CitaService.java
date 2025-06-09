package com.agend.agendamientoservice.service;

import com.agend.agendamientoservice.DTOs.CitaRequest;
import com.agend.agendamientoservice.model.Cita;
import com.agend.agendamientoservice.model.Mascota;
import com.agend.agendamientoservice.model.Propietario;
import com.agend.agendamientoservice.model.Veterinario;
import com.agend.agendamientoservice.repository.CitaRepository;
import com.agend.agendamientoservice.repository.MascotaRepository;
import com.agend.agendamientoservice.repository.PropietarioRepository;
import com.agend.agendamientoservice.repository.VeterinarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class CitaService {
    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

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

    public Cita convertirYGuardar(CitaRequest request) {
        Cita cita = new Cita();
        cita.setMotivo(request.getMotivo());
        cita.setEstado("ACTIVO");

        try {
            // Formateadores tolerantes (hora puede tener o no segundos)
            DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("H:mm[:ss]");

            // Parseo seguro
            LocalDate fecha = LocalDate.parse(request.getFecha(), fechaFormatter);
            LocalTime hora = LocalTime.parse(request.getHora(), horaFormatter);

            cita.setFecha(java.sql.Date.valueOf(fecha));
            cita.setHora(java.sql.Time.valueOf(hora));

        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de fecha u hora inválido: " + e.getMessage());
        }


        // Relacionar entidades
        Mascota mascota = mascotaRepository.findById(request.getMascotaId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        Veterinario veterinario = veterinarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        // Obtener propietario desde la mascota
        Propietario propietario = mascota.getPropietario(); // suponiendo que Mascota tiene getPropietario()

        cita.setMascota(mascota);
        cita.setVeterinario(veterinario);
        cita.setPropietario(propietario);

        return citaRepository.save(cita);
    }

}