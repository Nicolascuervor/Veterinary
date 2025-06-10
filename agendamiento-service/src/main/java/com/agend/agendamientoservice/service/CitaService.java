package com.agend.agendamientoservice.service;

import com.agend.agendamientoservice.DTOs.CitaRequest;
import com.agend.agendamientoservice.controller.DisponibilidadHoraria;
import com.agend.agendamientoservice.model.*;
import com.agend.agendamientoservice.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
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

    public Cita convertirYGuardar(CitaRequest request) {
        Cita cita = new Cita();
        cita.setMotivo(request.getMotivo());
        cita.setEstado(Cita.EstadoCita.ACTIVO);

        LocalDate fecha = LocalDate.parse(request.getFecha());
        LocalTime hora = LocalTime.parse(request.getHora());

        cita.setFecha(fecha);
        cita.setHora(hora);


        Mascota mascota = mascotaRepository.findById(request.getMascotaId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        Veterinario vet = veterinarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
        Propietario propietario = mascota.getPropietario();

        cita.setMascota(mascota);
        cita.setVeterinario(vet);
        cita.setPropietario(propietario);

        // 🔐 Bloquear franja
        DisponibilidadHoraria franja = disponibilidadHorariaRepository
                .findByVeterinarioIdAndDiaAndHoraInicio(vet.getId(), fecha.getDayOfWeek(), hora)
                .orElseThrow(() -> new RuntimeException("Franja horaria no encontrada"));

        if (franja.getEstado() != EstadoDisponibilidad.DISPONIBLE) {
            throw new RuntimeException("La franja ya está ocupada o no está disponible");
        }

        franja.setEstado(EstadoDisponibilidad.OCUPADA);
        disponibilidadHorariaRepository.save(franja);

        return citaRepository.save(cita);
    }

    public void cancelarCita(Long citaId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        cita.setEstado(Cita.EstadoCita.CANCELADA);

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



}