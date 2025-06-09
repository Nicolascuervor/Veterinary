package com.agend.agendamientoservice.controller;

import com.agend.agendamientoservice.model.Mascota;
import com.agend.agendamientoservice.service.MascotaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mascotas")
public class MascotaController {
    @Autowired
    private MascotaService mascotaService;

    @GetMapping
    public List<Mascota> findAll() {
        return mascotaService.findAllMascotas();
    }


    @GetMapping("/propietario/{propietarioId}")
    public ResponseEntity<List<Mascota>> findByPropietarioId(@PathVariable Long propietarioId) {
        List<Mascota> mascotas = mascotaService.findByPropietarioId(propietarioId);
        if (mascotas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(mascotas);
    }

    @PostMapping("/registrar")
    public Mascota guardarMascota(@RequestBody Mascota mascota) {
        return mascotaService.guardar(mascota);
    }

    @GetMapping("/{id}")
    public Optional<Mascota> findMascotaById(@PathVariable Long id) {
        return mascotaService.findMascotaById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> actualizarMascota(@PathVariable Long id, @RequestBody Mascota mascota) {
        Optional<Mascota> existente = mascotaService.findMascotaById(id);
        if (existente.isPresent()) {
            mascota.setId(id);
            Mascota actualizado = mascotaService.guardar(mascota);
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarMascota(@PathVariable Long id) {
        try {
            mascotaService.eliminar(id);
            return new ResponseEntity<>(Map.of("eliminado", true), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
