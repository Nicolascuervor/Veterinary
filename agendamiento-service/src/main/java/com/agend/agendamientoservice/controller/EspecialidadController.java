package com.agend.agendamientoservice.controller;


import com.agend.agendamientoservice.model.Especialidad;
import com.agend.agendamientoservice.service.EspecialidadService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/especialidad")
public class EspecialidadController {
    @Autowired
    private EspecialidadService especialidadService;

    @GetMapping
    public List<Especialidad> findAll() {
        return especialidadService.findAllEspecialidads();
    }

    @PostMapping("/registrar")
    public Especialidad guardarEspecialidad(@RequestBody Especialidad especialidad) {
        return especialidadService.guardar(especialidad);
    }

    @GetMapping("/{id}")
    public Optional<Especialidad> findEspecialidadById(@PathVariable Long id) {
        return especialidadService.findEspecialidadById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Especialidad> actualizarEspecialidad(@PathVariable Long id, @RequestBody Especialidad especialidad) {
        Optional<Especialidad> existente = especialidadService.findEspecialidadById(id);
        if (existente.isPresent()) {
            especialidad.setId(id);
            Especialidad actualizado = especialidadService.guardar(especialidad);
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarEspecialidad(@PathVariable Long id) {
        try {
            especialidadService.eliminar(id);
            return new ResponseEntity<>(Map.of("eliminado", true), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
