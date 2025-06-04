package com.perfil.perfilservice.repository;

import com.perfil.perfilservice.model.PreferenciaPrivacidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreferenciaPrivacidadRepository extends JpaRepository<PreferenciaPrivacidad, Long> {

    List<PreferenciaPrivacidad> findByPerfilUsuarioId(Long perfilUsuarioId);

    Optional<PreferenciaPrivacidad> findByPerfilUsuarioIdAndTipoPreferencia(Long perfilUsuarioId, String tipoPreferencia);

    @Query("SELECT p FROM PreferenciaPrivacidad p WHERE p.perfilUsuario.id = :perfilUsuarioId AND p.valor = true")
    List<PreferenciaPrivacidad> findPreferenciasActivasByPerfilUsuarioId(@Param("perfilUsuarioId") Long perfilUsuarioId);

    void deleteByPerfilUsuarioIdAndTipoPreferencia(Long perfilUsuarioId, String tipoPreferencia);
}
