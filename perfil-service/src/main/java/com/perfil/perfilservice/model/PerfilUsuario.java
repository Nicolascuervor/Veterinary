package com.perfil.perfilservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "perfiles_usuario")
public class PerfilUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId; // ID del usuario en el authentication-service

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Email(message = "El formato del email no es válido")
    @Column(name = "email", unique = true)
    private String email;

    @Pattern(regexp = "\\d{10}", message = "El teléfono debe tener 10 dígitos")
    @Column(name = "telefono")
    private String telefono;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    @Column(name = "direccion")
    private String direccion;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero")
    private Genero genero;

    @Column(name = "ocupacion")
    private String ocupacion;

    @Column(name = "cedula", unique = true)
    private String cedula;

    @Column(name = "foto_perfil_url")
    private String fotoPerfilUrl;

    @Column(name = "biografia", length = 500)
    private String biografia;

    @Column(name = "preferencias_notificacion")
    private String preferenciasNotificacion; // JSON string

    @Column(name = "estado_perfil")
    @Enumerated(EnumType.STRING)
    private EstadoPerfil estadoPerfil = EstadoPerfil.ACTIVO;

    @Column(name = "fecha_ultima_actualizacion")
    private LocalDateTime fechaUltimaActualizacion;

    @OneToMany(mappedBy = "perfilUsuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ContactoEmergencia> contactosEmergencia;

    @OneToMany(mappedBy = "perfilUsuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PreferenciaPrivacidad> preferenciasPrivacidad;




    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.fechaUltimaActualizacion = LocalDateTime.now();
        if (this.estadoPerfil == null) {
            this.estadoPerfil = EstadoPerfil.ACTIVO;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.fechaUltimaActualizacion = LocalDateTime.now();
    }

    // Métodos de utilidad
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellido;
    }

    public boolean estaCompleto() {
        return nombre != null && !nombre.trim().isEmpty() &&
                apellido != null && !apellido.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                telefono != null && !telefono.trim().isEmpty();
    }
}

