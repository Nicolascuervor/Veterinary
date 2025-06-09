package com.agend.agendamientoservice.controller;


import com.agend.agendamientoservice.model.Servicio;
import com.agend.agendamientoservice.service.ServicioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/servicios")

public class ServicioController {

    @Autowired
    private ServicioService servicioService;



    @GetMapping("/veterinario/{id}")
    public ResponseEntity<List<Servicio>> obtenerServiciosPorVeterinario(@PathVariable Long id) {
        List<Servicio> servicios = servicioService.obtenerServiciosPorVeterinario(id);
        return ResponseEntity.ok(servicios);
    }

    @GetMapping
    public List<Servicio> findAll() {
        return servicioService.findAllServicios();
    }

    @PostMapping("/registrar")
    public Servicio guardarServicio(@RequestBody Servicio Servicio) {
        return servicioService.guardar(Servicio);
    }

    @GetMapping("/{id}")
    public Optional<Servicio> findServicioById(@PathVariable Long id) {
        return servicioService.findServicioById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Servicio> actualizarServicio(@PathVariable Long id, @RequestBody Servicio Servicio) {
        Optional<Servicio> existente = servicioService.findServicioById(id);
        if (existente.isPresent()) {
            Servicio.setId(id);
            Servicio actualizado = servicioService.guardar(Servicio);
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarServicio(@PathVariable Long id) {
        try {
            servicioService.eliminar(id);
            return new ResponseEntity<>(Map.of("eliminado", true), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}