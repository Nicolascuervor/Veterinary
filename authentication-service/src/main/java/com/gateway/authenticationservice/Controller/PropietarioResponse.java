package com.gateway.authenticationservice.Controller;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PropietarioResponse {
    private String nombre;
    private String apellido;
    private String direccion;
    private String telefono;
    private String cedula;
    private Long id;


}
