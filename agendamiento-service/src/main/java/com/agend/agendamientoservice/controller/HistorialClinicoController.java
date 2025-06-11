package com.agend.agendamientoservice.controller;


import com.agend.agendamientoservice.model.HistorialClinico;
import com.agend.agendamientoservice.service.HistorialClinicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/historial")
public class HistorialClinicoController {

    @Autowired
    private HistorialClinicoService historialService;

    @GetMapping("/mascota/{mascotaId}")
    public List<HistorialClinico> obtenerHistorialPorMascota(@PathVariable Long mascotaId) {
        return historialService.obtenerHistorialPorMascota(mascotaId);
    }

    @PostMapping("/nuevo")
    public HistorialClinico guardarHistorial(@RequestBody HistorialClinico historial) {
        return historialService.guardar(historial);
    }
}