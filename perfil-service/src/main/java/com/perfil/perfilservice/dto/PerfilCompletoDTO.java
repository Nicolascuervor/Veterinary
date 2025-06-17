package com.perfil.perfilservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilCompletoDTO {
    private PerfilUsuarioDTO perfil;
    private List<MascotaDTO> mascotas;
    private List<ContactoEmergenciaDTO> contactosEmergencia;
    private List<PreferenciaPrivacidadDTO> preferenciasPrivacidad;


}