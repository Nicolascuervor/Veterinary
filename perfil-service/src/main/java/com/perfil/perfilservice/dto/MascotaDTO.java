package com.perfil.perfilservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MascotaDTO {
    private Integer id;
    private String nombre;
    private String especie;
    private String raza;
    private String fotoUrl;

}