package com.perfil.perfilservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "preferencias_privacidad")
public class PreferenciaPrivacidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_usuario_id", nullable = false)
    @JsonIgnore
    private PerfilUsuario perfilUsuario;

    @Column(name = "tipo_preferencia", nullable = false)
    private String tipoPreferencia; // Ej: "MOSTRAR_EMAIL", "MOSTRAR_TELEFONO", "RECIBIR_MARKETING"

    @Column(name = "valor", nullable = false)
    private Boolean valor; // true/false para la preferencia

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
