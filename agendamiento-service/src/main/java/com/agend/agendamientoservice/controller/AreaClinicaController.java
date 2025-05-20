package com.agend.agendamientoservice.controller;


import com.agend.agendamientoservice.model.AreaClinica;
import com.agend.agendamientoservice.service.AreaClinicaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/AreaClinica")
public class AreaClinicaController {

    @Autowired
    private AreaClinicaService areaclinicaService;

    @GetMapping
    public List<AreaClinica> findAll() {
        return areaclinicaService.findAllAreaClinicas();
    }

    @PostMapping("/registrar")
    public AreaClinica guardarAreaClinica(@RequestBody AreaClinica areaclinica) {
        return areaclinicaService.guardar(areaclinica);
    }

    @GetMapping("/{id}")
    public Optional<AreaClinica> findAreaClinicaById(@PathVariable Long id) {
        return areaclinicaService.findAreaClinicaById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AreaClinica> actualizarAreaClinica(@PathVariable Long id, @RequestBody AreaClinica areaclinica) {
        Optional<AreaClinica> existente = areaclinicaService.findAreaClinicaById(id);
        if (existente.isPresent()) {
            areaclinica.setId(id);
            AreaClinica actualizado = areaclinicaService.guardar(areaclinica);
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarAreaClinica(@PathVariable Long id) {
        try {
            areaclinicaService.eliminar(id);
            return new ResponseEntity<>(Map.of("eliminado", true), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}