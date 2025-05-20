package com.agend.agendamientoservice.controller;

import com.agend.agendamientoservice.model.AgendaVeterinario;
import com.agend.agendamientoservice.service.AgendaVeterinarioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/agenda")

public class AgendaVeterinarioController {

    @Autowired
    private AgendaVeterinarioService agendaveterinarioService;

    @GetMapping
    public List<AgendaVeterinario> findAll() {
        return agendaveterinarioService.findAllAgendaVeterinarios();
    }

    @PostMapping("/registrar")
    public AgendaVeterinario guardarAgendaVeterinario(@RequestBody AgendaVeterinario agendaveterinario) {
        return agendaveterinarioService.guardar(agendaveterinario);
    }

    @GetMapping("/{id}")
    public Optional<AgendaVeterinario> findAgendaVeterinarioById(@PathVariable Long id) {
        return agendaveterinarioService.findAgendaVeterinarioById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgendaVeterinario> actualizarAgendaVeterinario(@PathVariable Long id, @RequestBody AgendaVeterinario agendaveterinario) {
        Optional<AgendaVeterinario> existente = agendaveterinarioService.findAgendaVeterinarioById(id);
        if (existente.isPresent()) {
            agendaveterinario.setId(id);
            AgendaVeterinario actualizado = agendaveterinarioService.guardar(agendaveterinario);
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarAgendaVeterinario(@PathVariable Long id) {
        try {
            agendaveterinarioService.eliminar(id);
            return new ResponseEntity<>(Map.of("eliminado", true), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
