package com.gateway.authenticationservice.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter

public class PropietarioRequest {
    public String nombre;
    public String apellido;
    public String direccion;
    public String telefono;
    public String cedula;
    public Long usuarioId;
}
