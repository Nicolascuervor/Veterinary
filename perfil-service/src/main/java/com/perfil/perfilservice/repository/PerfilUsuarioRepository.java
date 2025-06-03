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

