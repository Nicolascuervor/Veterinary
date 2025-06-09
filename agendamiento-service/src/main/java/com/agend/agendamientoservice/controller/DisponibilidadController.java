package com.agend.agendamientoservice.controller;


import com.agend.agendamientoservice.DTOs.DisponibilidadHorariaDTO;
import com.agend.agendamientoservice.service.DisponibilidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/disponibilidad")
@RequiredArgsConstructor
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    @GetMapping("/veterinario/{id}")
    public List<DisponibilidadHorariaDTO> obtenerDisponibilidad(
            @PathVariable Long id,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        System.out.println("[DisponibilidadController] Consulta para veterinario ID: " + id + ", fecha: " + fecha);
        List<DisponibilidadHorariaDTO> disponibles = disponibilidadService.obtenerDisponibilidad(id, fecha);
        System.out.println("[DisponibilidadController] Resultados devueltos: " + disponibles.size());
        return disponibles;
    }
}