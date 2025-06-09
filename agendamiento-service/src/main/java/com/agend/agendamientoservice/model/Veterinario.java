package com.agend.agendamientoservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "veterinarios")
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "apellido")
    private String apellido;

    @Column(name = "cedula")
    private String dni;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "correo")
    private String correo;

    @ManyToOne
    @JoinColumn(name = "especialidad_id", referencedColumnName = "id")
    private Especialidad especialidad;

    @JsonIgnore // Evita serializar agendas dentro del veterinario
    @OneToMany(mappedBy = "veterinario", cascade = CascadeType.ALL)
    private List<AgendaVeterinario> agendas;


    @JsonIgnore
    @OneToMany(mappedBy = "veterinario")
    private List<Cita> citas;



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