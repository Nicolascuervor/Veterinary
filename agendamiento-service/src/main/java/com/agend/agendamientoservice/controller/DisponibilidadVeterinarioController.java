package com.agend.agendamientoservice.controller;

import com.agend.agendamientoservice.model.DisponibilidadVeterinario;
import com.agend.agendamientoservice.service.DisponibilidadVeterinarioService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/disponibilidad")
public class DisponibilidadVeterinarioController {
    @Autowired
    private DisponibilidadVeterinarioService disponibilidadveterinarioService;

    @GetMapping
    public List<DisponibilidadVeterinario> findAll() {
        return disponibilidadveterinarioService.findAllDisponibilidadVeterinarios();
    }

    @PostMapping("/registrar")
    public DisponibilidadVeterinario guardarDisponibilidadVeterinario(@RequestBody DisponibilidadVeterinario disponibilidadveterinario) {
        return disponibilidadveterinarioService.guardar(disponibilidadveterinario);
    }

    @GetMapping("/{id}")
    public Optional<DisponibilidadVeterinario> findDisponibilidadVeterinarioById(@PathVariable Long id) {
        return disponibilidadveterinarioService.findDisponibilidadVeterinarioById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DisponibilidadVeterinario> actualizarDisponibilidadVeterinario(@PathVariable Long id, @RequestBody DisponibilidadVeterinario disponibilidadveterinario) {
        Optional<DisponibilidadVeterinario> existente = disponibilidadveterinarioService.findDisponibilidadVeterinarioById(id);
        if (existente.isPresent()) {
            disponibilidadveterinario.setId(id);
            DisponibilidadVeterinario actualizado = disponibilidadveterinarioService.guardar(disponibilidadveterinario);
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarDisponibilidadVeterinario(@PathVariable Long id) {
        try {
            disponibilidadveterinarioService.eliminar(id);
            return new ResponseEntity<>(Map.of("eliminado", true), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
