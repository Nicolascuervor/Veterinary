package com.agend.agendamientoservice.controller;

import com.agend.agendamientoservice.model.Veterinario;
import com.agend.agendamientoservice.service.VeterinarioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/veterinarios")
public class VaterinarioController {
    @Autowired
    private VeterinarioService veterinarioService;

    @GetMapping
    public List<Veterinario> findAll() {
        return veterinarioService.findAllVeterinarios();
    }

    @PostMapping("/registrar")
    public Veterinario guardarVeterinario(@RequestBody Veterinario veterinario) {
        return veterinarioService.guardar(veterinario);
    }

    @GetMapping("/{id}")
    public Optional<Veterinario> findVeterinarioById(@PathVariable Long id) {
        return veterinarioService.findVeterinarioById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Veterinario> actualizarVeterinario(@PathVariable Long id, @RequestBody Veterinario veterinario) {
        Optional<Veterinario> existente = veterinarioService.findVeterinarioById(id);
        if (existente.isPresent()) {
            veterinario.setId(id);
            Veterinario actualizado = veterinarioService.guardar(veterinario);
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarVeterinario(@PathVariable Long id) {
        try {
            veterinarioService.eliminar(id);
            return new ResponseEntity<>(Map.of("eliminado", true), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
