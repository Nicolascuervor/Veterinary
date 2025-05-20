package com.agend.agendamientoservice.controller;


import com.agend.agendamientoservice.model.Propietario;
import com.agend.agendamientoservice.service.PropietarioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/propietarios")
public class PropietarioController {
    @Autowired
    private PropietarioService propietarioService;

    @GetMapping
    public List<Propietario> findAll() {
        return propietarioService.findAllPropietarios();
    }

    @PostMapping("/registrar")
    public Propietario guardarPropietario(@RequestBody Propietario propietario) {
        return propietarioService.guardar(propietario);
    }

    @GetMapping("/{id}")
    public Optional<Propietario> findPropietarioById(@PathVariable Long id) {
        return propietarioService.findPropietarioById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Propietario> actualizarPropietario(@PathVariable Long id, @RequestBody Propietario propietario) {
        Optional<Propietario> existente = propietarioService.findPropietarioById(id);
        if (existente.isPresent()) {
            propietario.setId(id);
            Propietario actualizado = propietarioService.guardar(propietario);
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarPropietario(@PathVariable Long id) {
        try {
            propietarioService.eliminar(id);
            return new ResponseEntity<>(Map.of("eliminado", true), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
