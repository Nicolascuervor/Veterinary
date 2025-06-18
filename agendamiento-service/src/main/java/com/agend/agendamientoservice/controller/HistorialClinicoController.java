package com.agend.agendamientoservice.controller;


import com.agend.agendamientoservice.DTOs.*;

import com.agend.agendamientoservice.service.HistorialClinicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/historial")
public class HistorialClinicoController {

    private final HistorialClinicoService historialService;

    @Autowired
    public HistorialClinicoController(HistorialClinicoService historialService) {
        this.historialService = historialService;
    }


    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<HistorialCompletoDTO> obtenerHistorialCompleto(@PathVariable Long mascotaId) {
        HistorialCompletoDTO historial = historialService.construirHistorialCompleto(mascotaId);
        return ResponseEntity.ok(historial);
    }


    @PostMapping("/encuentro/cita/{citaId}")
    public ResponseEntity<EncuentroClinicoDTO> crearEncuentroClinico(
            @PathVariable Long citaId,
            @RequestBody EncuentroClinicoCreateDTO encuentroData) {
        EncuentroClinicoDTO nuevoEncuentro = historialService.crearEncuentro(citaId, encuentroData);
        return new ResponseEntity<>(nuevoEncuentro, HttpStatus.CREATED);
    }


    @PostMapping("/vacunacion/mascota/{mascotaId}")
    public ResponseEntity<VacunacionDTO> registrarVacunacion(
            @PathVariable Long mascotaId,
            @RequestBody VacunacionCreateDTO vacunaData) {
        VacunacionDTO nuevaVacuna = historialService.registrarVacuna(mascotaId, vacunaData);
        return new ResponseEntity<>(nuevaVacuna, HttpStatus.CREATED);
    }
}

