package com.agend.agendamientoservice.service;


import com.agend.agendamientoservice.DTOs.*;
import com.agend.agendamientoservice.exception.ResourceNotFoundException;
import com.agend.agendamientoservice.model.Cita;
import com.agend.agendamientoservice.model.Mascota;
import com.agend.agendamientoservice.model.Vacunacion;
import com.agend.agendamientoservice.repository.MascotaRepository;
import com.agend.agendamientoservice.repository.VacunacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import com.agend.agendamientoservice.model.*;
import com.agend.agendamientoservice.repository.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;


@Service
public class HistorialClinicoService {

    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;
    private final VacunacionRepository vacunacionRepository;
    private final EncuentroClinicoRepository encuentroClinicoRepository;
    private final VeterinarioRepository veterinarioRepository;

    @Autowired
    public HistorialClinicoService(MascotaRepository mascotaRepository, CitaRepository citaRepository, VacunacionRepository vacunacionRepository, EncuentroClinicoRepository encuentroClinicoRepository, VeterinarioRepository veterinarioRepository) {
        this.mascotaRepository = mascotaRepository;
        this.citaRepository = citaRepository;
        this.vacunacionRepository = vacunacionRepository;
        this.encuentroClinicoRepository = encuentroClinicoRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    @Transactional(readOnly = true)
    public HistorialCompletoDTO construirHistorialCompleto(Long mascotaId) {
        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota con id " + mascotaId + " no encontrada."));

        // 1. Obtener perfil de la mascota
        PerfilMascotaDTO perfil = convertirMascotaAPerfilDTO(mascota);

        // 2. Obtener todas las citas y sus encuentros asociados
        List<Cita> citasDeMascota = citaRepository.findByMascotaIdOrderByFechaDesc(mascotaId);
        List<CitaHistorialDTO> citasDTO = citasDeMascota.stream()
                .map(this::convertirCitaAHistorialDTO)
                .collect(Collectors.toList());

        // 3. Obtener todas las vacunaciones
        List<Vacunacion> vacunaciones = vacunacionRepository.findByMascotaIdOrderByFechaAplicacionDesc(mascotaId);
        List<VacunacionDTO> vacunacionesDTO = vacunaciones.stream()
                .map(this::convertirVacunaADTO)
                .collect(Collectors.toList());

        // 4. Ensamblar el DTO final
        HistorialCompletoDTO historialCompleto = new HistorialCompletoDTO();
        historialCompleto.setPerfilMascota(perfil);
        historialCompleto.setCitas(citasDTO);
        historialCompleto.setVacunaciones(vacunacionesDTO);

        return historialCompleto;
    }

    @Transactional
    public EncuentroClinicoDTO crearEncuentro(Long citaId, EncuentroClinicoCreateDTO encuentroData) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita con id " + citaId + " no encontrada."));

        if (cita.getEstadoCita() != EstadoCita.REALIZADA) {
            throw new IllegalStateException("Solo se puede crear un encuentro clínico para citas con estado 'REALIZADA'.");
        }
        if (cita.getEncuentroClinico() != null) {
            throw new IllegalStateException("La cita con id " + citaId + " ya tiene un encuentro clínico asociado.");
        }

        EncuentroClinico encuentro = new EncuentroClinico();
        encuentro.setCita(cita);
        encuentro.setFechaEncuentro(LocalDateTime.now());
        encuentro.setMotivoConsulta(encuentroData.getMotivoConsulta());
        encuentro.setAnamnesis(encuentroData.getAnamnesis());
        encuentro.setDiagnosticoPrincipal(encuentroData.getDiagnosticoPrincipal());
        encuentro.setTratamientoSugerido(encuentroData.getTratamientoSugerido());
        encuentro.setObservaciones(encuentroData.getObservaciones());

        if (encuentroData.getSignosVitales() != null) {
            SignosVitales signos = new SignosVitales();
            signos.setEncuentro(encuentro);
            signos.setPesoKg(encuentroData.getSignosVitales().getPesoKg());
            signos.setTemperaturaCelsius(encuentroData.getSignosVitales().getTemperaturaCelsius());
            signos.setFrecuenciaCardiaca(encuentroData.getSignosVitales().getFrecuenciaCardiaca());
            signos.setFrecuenciaRespiratoria(encuentroData.getSignosVitales().getFrecuenciaRespiratoria());
            encuentro.setSignosVitales(signos);
        }

        if (encuentroData.getPrescripciones() != null && !encuentroData.getPrescripciones().isEmpty()) {
            List<PrescripcionMedica> prescripciones = new ArrayList<>();
            for (PrescripcionMedicaDTO dto : encuentroData.getPrescripciones()) {
                PrescripcionMedica p = new PrescripcionMedica();
                p.setEncuentro(encuentro);
                p.setMedicamento(dto.getMedicamento());
                p.setDosis(dto.getDosis());
                p.setFrecuencia(dto.getFrecuencia());
                p.setDuracion(dto.getDuracion());
                p.setInstrucciones(dto.getInstrucciones());
                prescripciones.add(p);
            }
            encuentro.setPrescripciones(prescripciones);
        }

        EncuentroClinico nuevoEncuentro = encuentroClinicoRepository.save(encuentro);
        return convertirEncuentroADTO(nuevoEncuentro);
    }

    @Transactional
    public VacunacionDTO registrarVacuna(Long mascotaId, VacunacionCreateDTO vacunaData) {
        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota con id " + mascotaId + " no encontrada."));
        Veterinario veterinario = veterinarioRepository.findById(Long.valueOf(vacunaData.getVeterinarioId()))
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario con id " + vacunaData.getVeterinarioId() + " no encontrado."));

        Vacunacion vacuna = new Vacunacion();
        vacuna.setMascota(mascota);
        vacuna.setVeterinario(veterinario);
        vacuna.setNombreVacuna(vacunaData.getNombreVacuna());
        vacuna.setLote(vacunaData.getLote());
        vacuna.setFechaAplicacion(vacunaData.getFechaAplicacion());
        vacuna.setFechaProximaDosis(vacunaData.getFechaProximaDosis());

        Vacunacion nuevaVacuna = vacunacionRepository.save(vacuna);
        return convertirVacunaADTO(nuevaVacuna);
    }

    // --- MÉTODOS PRIVADOS DE CONVERSIÓN (MAPPERS) ---

    private PerfilMascotaDTO convertirMascotaAPerfilDTO(Mascota mascota) {
        PerfilMascotaDTO dto = new PerfilMascotaDTO();
        dto.setId(mascota.getId());
        dto.setNombre(mascota.getNombre());
        dto.setEspecie(mascota.getEspecie());
        dto.setRaza(mascota.getRaza());
        dto.setFechaNacimiento(mascota.getFechaNacimiento());
        dto.setSexo(mascota.getSexo());
        dto.setFotoUrl(mascota.getImageUrl());
        if (mascota.getPropietario() != null) {
            dto.setPropietarioNombre(mascota.getPropietario().getNombre() + " " + mascota.getPropietario().getApellido());
        }
        return dto;
    }


    private CitaHistorialDTO convertirCitaAHistorialDTO(Cita cita) {
        CitaHistorialDTO dto = new CitaHistorialDTO();
        dto.setId(cita.getId());


        if (cita.getFecha() != null && cita.getHora() != null) {
            dto.setFecha(LocalDateTime.of(cita.getFecha(), cita.getHora()));
        }

        dto.setEstadoCita(cita.getEstadoCita().toString());

        if (cita.getServicio() != null) {
            dto.setMotivoServicio(cita.getServicio().getNombre());
        } else {
            dto.setMotivoServicio(cita.getMotivo());
        }

        if (cita.getVeterinario() != null) {
            dto.setVeterinarioNombre(cita.getVeterinario().getNombre() + " " + cita.getVeterinario().getApellido());
        }

        if (cita.getEstadoCita() == EstadoCita.REALIZADA && cita.getEncuentroClinico() != null) {
            dto.setEncuentro(convertirEncuentroADTO(cita.getEncuentroClinico()));
        }
        return dto;
    }
    private EncuentroClinicoDTO convertirEncuentroADTO(EncuentroClinico encuentro) {
        if (encuentro == null) return null;
        EncuentroClinicoDTO dto = new EncuentroClinicoDTO();
        dto.setId(encuentro.getId());
        dto.setFechaEncuentro(encuentro.getFechaEncuentro());
        dto.setMotivoConsulta(encuentro.getMotivoConsulta());
        dto.setAnamnesis(encuentro.getAnamnesis());
        dto.setDiagnosticoPrincipal(encuentro.getDiagnosticoPrincipal());
        dto.setTratamientoSugerido(encuentro.getTratamientoSugerido());
        dto.setObservaciones(encuentro.getObservaciones());

        if (encuentro.getSignosVitales() != null) {
            SignosVitalesDTO svDto = new SignosVitalesDTO();
            svDto.setPesoKg(encuentro.getSignosVitales().getPesoKg());
            svDto.setTemperaturaCelsius(encuentro.getSignosVitales().getTemperaturaCelsius());
            svDto.setFrecuenciaCardiaca(encuentro.getSignosVitales().getFrecuenciaCardiaca());
            svDto.setFrecuenciaRespiratoria(encuentro.getSignosVitales().getFrecuenciaRespiratoria());
            dto.setSignosVitales(svDto);
        }

        if (encuentro.getPrescripciones() != null) {
            dto.setPrescripciones(encuentro.getPrescripciones().stream().map(p -> {
                PrescripcionMedicaDTO pDto = new PrescripcionMedicaDTO();
                pDto.setMedicamento(p.getMedicamento());
                pDto.setDosis(p.getDosis());
                pDto.setFrecuencia(p.getFrecuencia());
                pDto.setDuracion(p.getDuracion());
                pDto.setInstrucciones(p.getInstrucciones());
                return pDto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }

    private VacunacionDTO convertirVacunaADTO(Vacunacion vacuna) {
        VacunacionDTO dto = new VacunacionDTO();
        dto.setId(vacuna.getId());
        dto.setNombreVacuna(vacuna.getNombreVacuna());
        dto.setLote(vacuna.getLote());
        dto.setFechaAplicacion(vacuna.getFechaAplicacion());
        dto.setFechaProximaDosis(vacuna.getFechaProximaDosis());
        if (vacuna.getVeterinario() != null) {
            dto.setVeterinarioNombre(vacuna.getVeterinario().getNombre() + " " + vacuna.getVeterinario().getApellido());
        }
        return dto;
    }

}


