package com.perfil.perfilservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "contactos_emergencia")
public class ContactoEmergencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_usuario_id", nullable = false)
    @JsonIgnore
    private PerfilUsuario perfilUsuario;

    @NotBlank(message = "El nombre del contacto es obligatorio")
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @NotBlank(message = "El teléfono del contacto es obligatorio")
    @Pattern(regexp = "\\d{10}", message = "El teléfono debe tener 10 dígitos")
    @Column(name = "telefono", nullable = false)
    private String telefono;

    @NotBlank(message = "La relación es obligatoria")
    @Column(name = "relacion", nullable = false)
    private String relacion; // Ej: "Madre", "Padre", "Hermano", "Amigo"

    @Email(message = "El formato del email no es válido")
    @Column(name = "email")
    private String email;

    @Column(name = "es_contacto_principal")
    private Boolean esContactoPrincipal = false;

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
