package com.agend.agendamientoservice.DTOs;

import com.agend.agendamientoservice.model.Mascota;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Data // O usa getters/setters manuales
public class MascotaResponseDTO {
    private Long id;
    private String nombre;
    private String especie;
    private String raza;
    private LocalDate fechaNacimiento;
    private String edad;
    private String color;
    private Double peso;
    private String sexo;

    private String imageUrl;
    private PropietarioSimpleDTO propietario; // <-- ¡La clave está aquí!

    // Constructor para facilitar la conversión
    public MascotaResponseDTO(Mascota mascota) {
        this.id = mascota.getId();
        this.nombre = mascota.getNombre();
        this.especie = mascota.getEspecie();
        this.raza = mascota.getRaza();
        this.fechaNacimiento = mascota.getFechaNacimiento();
        this.edad = mascota.getEdad();
        this.color = mascota.getColor();
        this.peso = mascota.getPeso();
        this.sexo = mascota.getSexo();
        this.imageUrl = mascota.getImageUrl();
        if (mascota.getPropietario() != null) {
            this.propietario = new PropietarioSimpleDTO();
            this.propietario.setId(mascota.getPropietario().getId());
            this.propietario.setNombre(mascota.getPropietario().getNombre());
        }
    }

}
