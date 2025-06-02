package com.perfil.perfilservice.repository;

import com.perfil.perfilservice.model.PerfilUsuario;
import com.perfil.perfilservice.model.ContactoEmergencia;
import com.perfil.perfilservice.model.PreferenciaPrivacidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilUsuarioRepository extends JpaRepository<PerfilUsuario, Long> {

    Optional<PerfilUsuario> findByUserId(Long userId);

    Optional<PerfilUsuario> findByEmail(String email);

    Optional<PerfilUsuario> findByCedula(String cedula);

    List<PerfilUsuario> findByEstadoPerfil(String estadoPerfil);

    @Query("SELECT p FROM PerfilUsuario p WHERE " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.apellido) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<PerfilUsuario> buscarPorTermino(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(p) FROM PerfilUsuario p WHERE p.estadoPerfil = :estado")
    Long contarPorEstado(@Param("estado") String estado);

    boolean existsByEmail(String email);

    boolean existsByCedula(String cedula);

    boolean existsByUserId(Long userId);
}

@Repository
public interface ContactoEmergenciaRepository extends JpaRepository<ContactoEmergencia, Long> {

    List<ContactoEmergencia> findByPerfilUsuarioId(Long perfilUsuarioId);

    List<ContactoEmergencia> findByPerfilUsuarioIdAndEsContactoPrincipal(Long perfilUsuarioId, Boolean esContactoPrincipal);

    Optional<ContactoEmergencia> findByPerfilUsuarioIdAndId(Long perfilUsuarioId, Long contactoId);

    @Query("SELECT c FROM ContactoEmergencia c WHERE c.perfilUsuario.id = :perfilUsuarioId ORDER BY c.esContactoPrincipal DESC, c.nombre ASC")
    List<ContactoEmergencia> findByPerfilUsuarioIdOrdenado(@Param("perfilUsuarioId") Long perfilUsuarioId);

    void deleteByPerfilUsuarioIdAndId(Long perfilUsuarioId, Long contactoId);
}

@Repository
public interface PreferenciaPrivacidadRepository extends JpaRepository<PreferenciaPrivacidad, Long> {

    List<PreferenciaPrivacidad> findByPerfilUsuarioId(Long perfilUsuarioId);

    Optional<PreferenciaPrivacidad> findByPerfilUsuarioIdAndTipoPreferencia(Long perfilUsuarioId, String tipoPreferencia);

    @Query("SELECT p FROM PreferenciaPrivacidad p WHERE p.perfilUsuario.id = :perfilUsuarioId AND p.valor = true")
    List<PreferenciaPrivacidad> findPreferenciasActivasByPerfilUsuarioId(@Param("perfilUsuarioId") Long perfilUsuarioId);

    void deleteByPerfilUsuarioIdAndTipoPreferencia(Long perfilUsuarioId, String tipoPreferencia);
}