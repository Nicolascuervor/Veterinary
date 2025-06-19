package com.agend.agendamientoservice.DTOs;

import com.agend.agendamientoservice.model.Propietario;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Data
public class PropietarioResponseDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String direccion;
    private Long usuarioId;

    public PropietarioResponseDTO(Propietario propietario) {
        this.id = propietario.getId();
        this.nombre = propietario.getNombre();
        this.apellido = propietario.getApellido();
        this.dni = propietario.getDni();
        this.telefono = propietario.getTelefono();
        this.direccion = propietario.getDireccion();
        this.usuarioId = propietario.getUsuarioId();
    }
}