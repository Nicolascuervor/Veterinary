package com.agend.agendamientoservice.controller;

import com.agend.agendamientoservice.DTOs.CitaRequest;
import com.agend.agendamientoservice.model.Cita;
import com.agend.agendamientoservice.service.CitaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/cita")
public class CitaController {
    @Autowired
    private CitaService citaService;

    @GetMapping
    public List<Cita> findAll() {
        return citaService.findAllCitas();
    }

    @PostMapping("/registrar")
    public ResponseEntity<Cita> guardarCita(@RequestBody CitaRequest request) {
        Cita cita = citaService.convertirYGuardar(request);
        return ResponseEntity.ok(cita);
    }


    @GetMapping("/{id}")
    public Optional<Cita> findCitaById(@PathVariable Long id) {
        return citaService.findCitaById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> actualizarCita(@PathVariable Long id, @RequestBody Cita cita) {
        Optional<Cita> existente = citaService.findCitaById(id);
        if (existente.isPresent()) {
            cita.setId(id);
            Cita actualizado = citaService.guardar(cita);
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarCita(@PathVariable Long id) {
        try {
            citaService.eliminar(id);
            return new ResponseEntity<>(Map.of("eliminado", true), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
