package com.perfil.perfilservice.service;

import com.perfil.perfilservice.dto.*;
import com.perfil.perfilservice.model.*;
import com.perfil.perfilservice.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.client.RestTemplate;
import com.perfil.perfilservice.dto.PropietarioDTO; // Asegúrate de tener este DTO


@Service
@RequiredArgsConstructor
@Transactional
public class PerfilUsuarioService {

    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final ContactoEmergenciaRepository contactoEmergenciaRepository;
    private final PreferenciaPrivacidadRepository preferenciaPrivacidadRepository;
    private final EmailService emailService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${agendamiento.service.url}")
    private String agendamientoServiceUrl;

    @Value("${authentication.service.url}")
    private String authenticationServiceUrl;


    @Transactional
    public Optional<PerfilUsuarioDTO> obtenerYCrearPerfilPorUserId(Long userId) {
        // 1. Intenta obtener el perfil existente
        Optional<PerfilUsuario> perfilExistente = perfilUsuarioRepository.findByUserId(userId);

        if (perfilExistente.isPresent()) {
            // Si ya existe, lo convierte a DTO y lo devuelve
            return perfilExistente.map(this::convertirADTO);
        } else {
            // 2. Si NO existe, lo crea
            System.out.println("Perfil no encontrado para userId: " + userId + ". Creando uno nuevo...");

            // 2a. Llama a agendamiento-service para obtener los datos del propietario
            PropietarioDTO propietario = obtenerDatosPropietario(userId);

            // 2b. Crea una nueva entidad PerfilUsuario con esos datos
            PerfilUsuario nuevoPerfil = new PerfilUsuario();
            nuevoPerfil.setUserId(userId);
            nuevoPerfil.setNombre(propietario.getNombre());
            nuevoPerfil.setApellido(propietario.getApellido());
            nuevoPerfil.setEmail(propietario.getEmail());
            nuevoPerfil.setTelefono(propietario.getTelefono());
            nuevoPerfil.setDireccion(propietario.getDireccion());
            nuevoPerfil.setEstadoPerfil(EstadoPerfil.ACTIVO);

            // 2c. Guarda el nuevo perfil en la base de datos
            PerfilUsuario perfilGuardado = perfilUsuarioRepository.save(nuevoPerfil);

            // 2d. Crea preferencias y envía email (lógica que ya tenías)
            crearPreferenciasDefecto(perfilGuardado);
            emailService.enviarEmailBienvenidaPerfil(perfilGuardado);

            // 3. Devuelve el perfil recién creado
            return Optional.of(convertirADTO(perfilGuardado));
        }
    }

    /**
     * NUEVO: Método helper para comunicarse con agendamiento-service.
     */
    private PropietarioDTO obtenerDatosPropietario(Long authUserId) {
        // Este endpoint debe existir en agendamiento-service
        String url = agendamientoServiceUrl + "/propietarios/auth/" + authUserId;
        try {
            return restTemplate.getForObject(url, PropietarioDTO.class);
        } catch (Exception e) {
            // Manejar el caso en que no se pueda obtener el propietario
            throw new IllegalStateException("No se pudieron obtener los datos del propietario desde agendamiento-service para el usuario: " + authUserId, e);
        }
    }

    public List<MascotaDTO> findMascotasByAuthUserId(Long authUserId) {
        // Paso 1: Obtener el ID del Propietario desde agendamiento-service
        // (Asume que agendamiento-service tiene un endpoint para esto)
        String urlPropietario = agendamientoServiceUrl + "/propietarios/auth/" + authUserId;
        PropietarioDTO propietario = restTemplate.getForObject(urlPropietario, PropietarioDTO.class);

        if (propietario == null) {
            return Collections.emptyList();
        }

        // Paso 2: Usar el ID del Propietario para obtener sus mascotas
        String urlMascotas = agendamientoServiceUrl + "/mascotas/propietario/" + propietario.getId();
        ResponseEntity<List<MascotaDTO>> response = restTemplate.exchange(
                urlMascotas,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MascotaDTO>>() {}
        );
        return response.getBody();
    }

    public ResponseEntity<String> solicitarCambioPassword(CambioPasswordDTO dto) {
        String url = authenticationServiceUrl + "/auth/change-password";
        try {
            return restTemplate.postForEntity(url, dto, String.class);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        }
    }


    public PerfilCompletoDTO getPerfilCompleto(Long authUserId) {
        // 1. Obtener la entidad PerfilUsuario desde la base de datos
        PerfilUsuario perfilUsuario = perfilUsuarioRepository.findByUserId(authUserId)
                .orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado para el usuario con ID: " + authUserId));

        // 2. Convertir la entidad a su DTO correspondiente usando el método existente
        PerfilUsuarioDTO perfilDTO = convertirADTO(perfilUsuario);

        // 3. Obtener la lista de mascotas llamando al método que se comunica con agendamiento-service
        List<MascotaDTO> mascotas = findMascotasByAuthUserId(authUserId);

        // 4. Obtener los contactos de emergencia usando el método existente en este servicio
        List<ContactoEmergenciaDTO> contactos = obtenerContactosEmergencia(authUserId);

        // 5. Obtener las preferencias de privacidad usando el método existente
        List<PreferenciaPrivacidadDTO> preferencias = obtenerPreferenciasPrivacidad(authUserId);

        // 6. Ensamblar el DTO completo con toda la información recopilada
        PerfilCompletoDTO perfilCompleto = new PerfilCompletoDTO();
        perfilCompleto.setPerfil(perfilDTO);
        perfilCompleto.setMascotas(mascotas);
        perfilCompleto.setContactosEmergencia(contactos);

        // Aquí hay una inconsistencia en tu DTO original, lo ajustamos en el siguiente paso.
        // Por ahora, asumimos que el DTO aceptará una lista.
        // Si tu DTO solo acepta una, podrías tener que decidir cuál mostrar o cambiar el DTO.
        // La mejor práctica es que acepte la lista completa.
        perfilCompleto.setPreferenciasPrivacidad(preferencias); // Esto requiere un cambio en PerfilCompletoDTO

        return perfilCompleto;
    }


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

    public void updateFotoPerfil(Long authUserId, String fotoUrl) {

        PerfilUsuario perfil = perfilUsuarioRepository.findByUserId(authUserId)
                .orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado para el usuario: " + authUserId));

        // 2. Establecer la nueva URL en la entidad del perfil.
        perfil.setFotoPerfilUrl(fotoUrl);

        // 3. Guardar la entidad actualizada en la base de datos.
        //    Como el servicio es @Transactional, este guardado se confirma
        //    automáticamente al finalizar el método.
        perfilUsuarioRepository.save(perfil);
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

    @Transactional
    public Optional<PerfilUsuarioDTO> obtenerOCrearPerfil(Long userId, String email) {
        Optional<PerfilUsuario> perfilExistente = perfilUsuarioRepository.findByUserId(userId);

        if (perfilExistente.isPresent()) {
            return perfilExistente.map(this::convertirADTO);
        } else {
            // --- LÓGICA CORREGIDA ---
            System.out.println("Perfil no encontrado para userId: " + userId + ". Creando uno nuevo...");

            // 1. Llamamos a agendamiento-service para obtener nombre y apellido.
            PropietarioDTO propietario = obtenerDatosPropietario(userId);

            // 2. Creamos el nuevo perfil con TODOS los datos necesarios.
            PerfilUsuario nuevoPerfil = new PerfilUsuario();
            nuevoPerfil.setUserId(userId);
            nuevoPerfil.setEmail(email); // Del header
            nuevoPerfil.setNombre(propietario.getNombre()); // De agendamiento-service
            nuevoPerfil.setApellido(propietario.getApellido()); // De agendamiento-service
            // --- AÑADE ESTAS DOS LÍNEAS ---
            nuevoPerfil.setTelefono(propietario.getTelefono());
            nuevoPerfil.setDireccion(propietario.getDireccion());
            // ---------------------------------
            nuevoPerfil.setEstadoPerfil(EstadoPerfil.ACTIVO);

            // 3. Guardamos la entidad ahora sí válida.
            PerfilUsuario perfilGuardado = perfilUsuarioRepository.save(nuevoPerfil);

            // Lógica de bienvenida que ya tenías
            crearPreferenciasDefecto(perfilGuardado);
            // emailService.enviarEmailBienvenidaPerfil(perfilGuardado);

            return Optional.of(convertirADTO(perfilGuardado));
        }
    }





}

