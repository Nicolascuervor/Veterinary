package com.perfil.perfilservice.repository;

import com.perfil.perfilservice.model.ContactoEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactoEmergenciaRepository extends JpaRepository<ContactoEmergencia, Long> {

    List<ContactoEmergencia> findByPerfilUsuarioId(Long perfilUsuarioId);

    List<ContactoEmergencia> findByPerfilUsuarioIdAndEsContactoPrincipal(Long perfilUsuarioId, Boolean esContactoPrincipal);

    Optional<ContactoEmergencia> findByPerfilUsuarioIdAndId(Long perfilUsuarioId, Long contactoId);

    @Query("SELECT c FROM ContactoEmergencia c WHERE c.perfilUsuario.id = :perfilUsuarioId ORDER BY c.esContactoPrincipal DESC, c.nombre ASC")
    List<ContactoEmergencia> findByPerfilUsuarioIdOrdenado(@Param("perfilUsuarioId") Long perfilUsuarioId);

    void deleteByPerfilUsuarioIdAndId(Long perfilUsuarioId, Long contactoId);
}
