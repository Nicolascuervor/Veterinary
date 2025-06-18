package com.agend.agendamientoservice.service;

import com.agend.agendamientoservice.DTOs.CitaRequest;
import com.agend.agendamientoservice.DTOs.CitaResponseDTO;
import com.agend.agendamientoservice.controller.DisponibilidadHoraria;
import com.agend.agendamientoservice.model.*;
import com.agend.agendamientoservice.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalTime;
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
    private ServicioRepository servicioRepository;


    @Autowired
    private PropietarioRepository propietarioRepository;

    @Autowired
    private DisponibilidadHorariaRepository disponibilidadHorariaRepository;



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

    @Transactional
    public CitaResponseDTO crearCitaDesdeRequest(CitaRequest request) {
        // --- LÓGICA CORREGIDA Y COMPLETA ---
        Mascota mascota = mascotaRepository.findById(request.getMascotaId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada con ID: " + request.getMascotaId()));

        Veterinario veterinario = veterinarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado con ID: " + request.getVeterinarioId()));

        // Asumiendo que CitaRequest ahora tiene servicioId
        Servicio servicio = servicioRepository.findById(request.getServicioId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + request.getServicioId()));

        LocalDate fecha = LocalDate.parse(request.getFecha());
        LocalTime hora = LocalTime.parse(request.getHora());

        // Bloqueo de la franja horaria (tu lógica existente está bien)
        DisponibilidadHoraria franja = disponibilidadHorariaRepository
                .findByVeterinarioIdAndDiaAndHoraInicio(veterinario.getId(), fecha.getDayOfWeek(), hora)
                .orElseThrow(() -> new RuntimeException("Franja horaria no encontrada"));
        if (franja.getEstado() != EstadoDisponibilidad.DISPONIBLE) {
            throw new RuntimeException("La franja ya está ocupada o no está disponible");
        }
        franja.setEstado(EstadoDisponibilidad.OCUPADA);
        disponibilidadHorariaRepository.save(franja);

        // Creación de la nueva Cita
        Cita nuevaCita = new Cita();
        nuevaCita.setPropietario(mascota.getPropietario());
        nuevaCita.setMascota(mascota);
        nuevaCita.setVeterinario(veterinario);
        nuevaCita.setServicio(servicio);
        nuevaCita.setFecha(fecha);
        nuevaCita.setHora(hora);
        nuevaCita.setMotivo(request.getMotivo());
        nuevaCita.setEstadoCita(EstadoCita.PENDIENTE);

        Cita citaGuardada = citaRepository.save(nuevaCita);
        return convertirCitaAResponseDTO(citaGuardada); // Devuelve el DTO para evitar recursividad
    }


    public void cancelarCita(Long citaId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        cita.setEstadoCita(EstadoCita.CANCELADA_PROPIETARIO);

        DisponibilidadHoraria franja = disponibilidadHorariaRepository
                .findByVeterinarioIdAndDiaAndHoraInicio(
                        cita.getVeterinario().getId(),
                        cita.getFecha().getDayOfWeek(), // ahora sí es LocalDate
                        cita.getHora()                  // ahora es LocalTime
                ).orElse(null);


        if (franja != null) {
            franja.setEstado(EstadoDisponibilidad.DISPONIBLE);
            disponibilidadHorariaRepository.save(franja);
        }

        citaRepository.save(cita);
    }


    private CitaResponseDTO convertirCitaAResponseDTO(Cita cita) {
        CitaResponseDTO dto = new CitaResponseDTO();
        dto.setId(cita.getId());
        dto.setMotivo(cita.getMotivo());
        dto.setFecha(cita.getFecha());
        dto.setHora(cita.getHora());
        dto.setEstadoCita(cita.getEstadoCita().toString());
        if (cita.getMascota() != null) {
            dto.setMascotaNombre(cita.getMascota().getNombre());
        }
        if (cita.getVeterinario() != null) {
            dto.setVeterinarioNombre(cita.getVeterinario().getNombre() + " " + cita.getVeterinario().getApellido());
        }
        if (cita.getServicio() != null) {
            dto.setServicioNombre(cita.getServicio().getNombre());
        }
        return dto;
    }



}