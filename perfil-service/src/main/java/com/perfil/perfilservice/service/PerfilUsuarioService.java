package com.perfil.perfilservice.service;

import com.perfil.perfilservice.dto.*;
import com.perfil.perfilservice.model.*;
import com.perfil.perfilservice.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PerfilUsuarioService {

    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final ContactoEmergenciaRepository contactoEmergenciaRepository;
    private final PreferenciaPrivacidadRepository preferenciaPrivacidadRepository;
    private final EmailService emailService;

    // Crear perfil
    public PerfilUsuarioDTO crearPerfil(PerfilUsuarioCreateDTO createDTO) {
        // Verificar que no exista ya un perfil para este usuario
        if (perfilUsuarioRepository.existsByUserId(createDTO.getUserId())) {
            throw new IllegalArgumentException("Ya existe un perfil para este usuario");
        }

        // Verificar email único
        if (createDTO.getEmail() != null && perfilUsuarioRepository.existsByEmail(createDTO.getEmail())) {
            throw new IllegalArgumentException("El email ya está en uso");
        }

        // Verificar cédula única
        if (createDTO.getCedula() != null && perfilUsuarioRepository.existsByCedula(createDTO.getCedula())) {
            throw new IllegalArgumentException("La cédula ya está registrada");
        }

        PerfilUsuario perfil = new PerfilUsuario();
        perfil.setUserId(createDTO.getUserId());
        perfil.setNombre(createDTO.getNombre());
        perfil.setApellido(createDTO.getApellido());
        perfil.setEmail(createDTO.getEmail());
        perfil.setTelefono(createDTO.getTelefono());
        perfil.setDireccion(createDTO.getDireccion());
        perfil.setFechaNacimiento(createDTO.getFechaNacimiento());

        if (createDTO.getGenero() != null) {
            perfil.setGenero(Genero.valueOf(createDTO.getGenero().toUpperCase()));
        }

        perfil.setOcupacion(createDTO.getOcupacion());
        perfil.setCedula(createDTO.getCedula());
        perfil.setEstadoPerfil(EstadoPerfil.ACTIVO);

        PerfilUsuario perfilGuardado = perfilUsuarioRepository.save(perfil);

        // Crear preferencias por defecto
        crearPreferenciasDefecto(perfilGuardado);

        // Enviar email de bienvenida
        emailService.enviarEmailBienvenidaPerfil(perfilGuardado);

        return convertirADTO(perfilGuardado);
    }

    // Obtener perfil por ID de usuario
    @Transactional(readOnly = true)
    public Optional<PerfilUsuarioDTO> obtenerPerfilPorUserId(Long userId) {
        return perfilUsuarioRepository.findByUserId(userId)
                .map(this::convertirADTO);
    }

    // Obtener perfil por ID
    @Transactional(readOnly = true)
    public Optional<PerfilUsuarioDTO> obtenerPerfilPorId(Long id) {
        return perfilUsuarioRepository.findById(id)
                .map(this::convertirADTO);
    }

    // Actualizar perfil
    public PerfilUsuarioDTO actualizarPerfil(Long userId, PerfilUsuarioUpdateDTO updateDTO) {
        PerfilUsuario perfil = perfilUsuarioRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado para el usuario: " + userId));

        // Verificar email único si se está cambiando
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(perfil.getEmail())) {
            if (perfilUsuarioRepository.existsByEmail(updateDTO.getEmail())) {
                throw new IllegalArgumentException("El email ya está en uso");
            }
        }

        // Verificar cédula única si se está cambiando
        if (updateDTO.getCedula() != null && !updateDTO.getCedula().equals(perfil.getCedula())) {
            if (perfilUsuarioRepository.existsByCedula(updateDTO.getCedula())) {
                throw new IllegalArgumentException("La cédula ya está registrada");
            }
        }

        // Actualizar campos no nulos
        if (updateDTO.getNombre() != null) perfil.setNombre(updateDTO.getNombre());
        if (updateDTO.getApellido() != null) perfil.setApellido(updateDTO.getApellido());
        if (updateDTO.getEmail() != null) perfil.setEmail(updateDTO.getEmail());
        if (updateDTO.getTelefono() != null) perfil.setTelefono(updateDTO.getTelefono());
        if (updateDTO.getDireccion() != null) perfil.setDireccion(updateDTO.getDireccion());
        if (updateDTO.getFechaNacimiento() != null) perfil.setFechaNacimiento(updateDTO.getFechaNacimiento());
        if (updateDTO.getGenero() != null) perfil.setGenero(Genero.valueOf(updateDTO.getGenero().toUpperCase()));
        if (updateDTO.getOcupacion() != null) perfil.setOcupacion(updateDTO.getOcupacion());
        if (updateDTO.getCedula() != null) perfil.setCedula(updateDTO.getCedula());
        if (updateDTO.getFotoPerfilUrl() != null) perfil.setFotoPerfilUrl(updateDTO.getFotoPerfilUrl());
        if (updateDTO.getBiografia() != null) perfil.setBiografia(updateDTO.getBiografia());

        PerfilUsuario perfilActualizado = perfilUsuarioRepository.save(perfil);
        return convertirADTO(perfilActualizado);
    }

    // Eliminar perfil (soft delete)
    public void eliminarPerfil(Long userId) {
        PerfilUsuario perfil = perfilUsuarioRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado para el usuario: " + userId));

        perfil.setEstadoPerfil(EstadoPerfil.ELIMINADO);
        perfilUsuarioRepository.save(perfil);
    }

    // Buscar perfiles
    @Transactional(readOnly = true)
    public List<PerfilResumenDTO> buscarPerfiles(String termino) {
        List<PerfilUsuario> perfiles = perfilUsuarioRepository.buscarPorTermino(termino);
        return perfiles.stream()
                .filter(p -> p.getEstadoPerfil() == EstadoPerfil.ACTIVO)
                .map(this::convertirAResumenDTO)
                .collect(Collectors.toList());
    }

    // Gestión de contactos de emergencia
    public ContactoEmergenciaDTO agregarContactoEmergencia(Long userId, ContactoEmergenciaDTO contactoDTO) {
        PerfilUsuario perfil = perfilUsuarioRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado"));

        ContactoEmergencia contacto = new ContactoEmergencia();
        contacto.setPerfilUsuario(perfil);
        contacto.setNombre(contactoDTO.getNombre());
        contacto.setTelefono(contactoDTO.getTelefono());
        contacto.setRelacion(contactoDTO.getRelacion());
        contacto.setEmail(contactoDTO.getEmail());
        contacto.setEsContactoPrincipal(contactoDTO.getEsContactoPrincipal());

        ContactoEmergencia contactoGuardado = contactoEmergenciaRepository.save(contacto);
        return convertirContactoADTO(contactoGuardado);
    }

    @Transactional(readOnly = true)
    public List<ContactoEmergenciaDTO> obtenerContactosEmergencia(Long userId) {
        PerfilUsuario perfil = perfilUsuarioRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado"));

        return contactoEmergenciaRepository.findByPerfilUsuarioIdOrdenado(perfil.getId())
                .stream()
                .map(this::convertirContactoADTO)
                .collect(Collectors.toList());
    }

    // Gestión de preferencias de privacidad
    public PreferenciaPrivacidadDTO actualizarPreferencia(Long userId, String tipoPreferencia, Boolean valor) {
        PerfilUsuario perfil = perfilUsuarioRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado"));

        PreferenciaPrivacidad preferencia = preferenciaPrivacidadRepository
                .findByPerfilUsuarioIdAndTipoPreferencia(perfil.getId(), tipoPreferencia)
                .orElse(new PreferenciaPrivacidad());

        preferencia.setPerfilUsuario(perfil);
        preferencia.setTipoPreferencia(tipoPreferencia);
        preferencia.setValor(valor);

        PreferenciaPrivacidad preferenciaGuardada = preferenciaPrivacidadRepository.save(preferencia);
        return convertirPreferenciaADTO(preferenciaGuardada);
    }

    @Transactional(readOnly = true)
    public List<PreferenciaPrivacidadDTO> obtenerPreferenciasPrivacidad(Long userId) {
        PerfilUsuario perfil = perfilUsuarioRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado"));

        return preferenciaPrivacidadRepository.findByPerfilUsuarioId(perfil.getId())
                .stream()
                .map(this::convertirPreferenciaADTO)
                .collect(Collectors.toList());
    }

    // Estadísticas (para admin)
    @Transactional(readOnly = true)
    public EstadisticasPerfilDTO obtenerEstadisticas() {
        Long totalPerfiles = perfilUsuarioRepository.count();
        Long perfilesActivos = perfilUsuarioRepository.contarPorEstado("ACTIVO");
        Long perfilesInactivos = perfilUsuarioRepository.contarPorEstado("INACTIVO");

        List<PerfilUsuario> todosPerfiles = perfilUsuarioRepository.findAll();
        Long perfilesCompletos = todosPerfiles.stream()
                .filter(PerfilUsuario::estaCompleto)
                .count();
        Long perfilesIncompletos = totalPerfiles - perfilesCompletos;

        Double porcentajeComplecion = totalPerfiles > 0 ?
                (perfilesCompletos.doubleValue() / totalPerfiles.doubleValue()) * 100 : 0.0;

        return new EstadisticasPerfilDTO(
                totalPerfiles,
                perfilesActivos,
                perfilesInactivos,
                perfilesCompletos,
                perfilesIncompletos,
                porcentajeComplecion
        );
    }

    // Métodos privados de utilidad
    private void crearPreferenciasDefecto(PerfilUsuario perfil) {
        String[] preferenciasDefecto = {
                "MOSTRAR_EMAIL",
                "MOSTRAR_TELEFONO",
                "RECIBIR_NOTIFICACIONES",
                "RECIBIR_MARKETING"
        };

        for (String tipoPreferencia : preferenciasDefecto) {
            PreferenciaPrivacidad preferencia = new PreferenciaPrivacidad();
            preferencia.setPerfilUsuario(perfil);
            preferencia.setTipoPreferencia(tipoPreferencia);
            preferencia.setValor(true); // Por defecto todas activas
            preferenciaPrivacidadRepository.save(preferencia);
        }
    }

    private PerfilUsuarioDTO convertirADTO(PerfilUsuario perfil) {
        PerfilUsuarioDTO dto = new PerfilUsuarioDTO();
        dto.setId(perfil.getId());
        dto.setNombre(perfil.getNombre());
        dto.setApellido(perfil.getApellido());
        dto.setEmail(perfil.getEmail());
        dto.setTelefono(perfil.getTelefono());
        dto.setDireccion(perfil.getDireccion());
        dto.setFechaNacimiento(perfil.getFechaNacimiento());
        dto.setGenero(perfil.getGenero() != null ? perfil.getGenero().name() : null);
        dto.setOcupacion(perfil.getOcupacion());
        dto.setCedula(perfil.getCedula());
        dto.setFotoPerfilUrl(perfil.getFotoPerfilUrl());
        dto.setBiografia(perfil.getBiografia());
        dto.setEstadoPerfil(perfil.getEstadoPerfil().name());
        dto.setFechaUltimaActualizacion(perfil.getFechaUltimaActualizacion());
        dto.setNombreCompleto(perfil.getNombreCompleto());
        dto.setPerfilCompleto(perfil.estaCompleto());

        // Cargar contactos y preferencias si es necesario
        if (perfil.getContactosEmergencia() != null) {
            dto.setContactosEmergencia(
                    perfil.getContactosEmergencia().stream()
                            .map(this::convertirContactoADTO)
                            .collect(Collectors.toList())
            );
        }

        if (perfil.getPreferenciasPrivacidad() != null) {
            dto.setPreferenciasPrivacidad(
                    perfil.getPreferenciasPrivacidad().stream()
                            .map(this::convertirPreferenciaADTO)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    private PerfilResumenDTO convertirAResumenDTO(PerfilUsuario perfil) {
        return new PerfilResumenDTO(
                perfil.getId(),
                perfil.getNombreCompleto(),
                perfil.getEmail(),
                perfil.getTelefono(),
                perfil.getFotoPerfilUrl(),
                perfil.getEstadoPerfil().name(),
                perfil.estaCompleto(),
                perfil.getFechaUltimaActualizacion()
        );
    }

    private ContactoEmergenciaDTO convertirContactoADTO(ContactoEmergencia contacto) {
        return new ContactoEmergenciaDTO(
                contacto.getId(),
                contacto.getNombre(),
                contacto.getTelefono(),
                contacto.getRelacion(),
                contacto.getEmail(),
                contacto.getEsContactoPrincipal()
        );
    }

    private PreferenciaPrivacidadDTO convertirPreferenciaADTO(PreferenciaPrivacidad preferencia) {
        return new PreferenciaPrivacidadDTO(
                preferencia.getId(),
                preferencia.getTipoPreferencia(),
                preferencia.getValor(),
                preferencia.getDescripcion()
        );
    }
}

