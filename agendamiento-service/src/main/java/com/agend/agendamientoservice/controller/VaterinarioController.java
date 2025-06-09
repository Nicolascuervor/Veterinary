package com.agend.agendamientoservice.controller;

import com.agend.agendamientoservice.model.Servicio;
import com.agend.agendamientoservice.model.Veterinario;
import com.agend.agendamientoservice.repository.ServicioRepository;
import com.agend.agendamientoservice.repository.VeterinarioRepository;
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

    @Autowired
    private final ServicioRepository servicioRepository;



    @Autowired
    private final VeterinarioRepository veterinarioRepository;

    public VaterinarioController(ServicioRepository servicioRepository, VeterinarioRepository veterinarioRepository) {
        this.servicioRepository = servicioRepository;
        this.veterinarioRepository = veterinarioRepository;
    }



    @GetMapping("/servicio/{servicioId}")
    public ResponseEntity<List<Veterinario>> obtenerVeterinariosPorServicio(@PathVariable Long servicioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        List<Veterinario> veterinarios = veterinarioRepository.findByEspecialidad(servicio.getEspecialidad());
        return ResponseEntity.ok(veterinarios);
    }


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
