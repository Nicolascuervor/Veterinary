package com.agend.agendamientoservice.controller;

import com.agend.agendamientoservice.model.Mascota;
import com.agend.agendamientoservice.model.Propietario;
import com.agend.agendamientoservice.service.FileStorageService;
import com.agend.agendamientoservice.service.MascotaService;
import com.agend.agendamientoservice.repository.PropietarioRepository; // Asegúrate de importar esto
import com.fasterxml.jackson.databind.ObjectMapper; // Importante
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mascotas")
public class MascotaController {

    @Autowired private MascotaService mascotaService;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private PropietarioRepository propietarioRepository;
    @Autowired private ObjectMapper objectMapper;

    // ... (los métodos GET, DELETE no cambian)

    @PostMapping("/registrar")
    public ResponseEntity<Mascota> guardarMascota(
            @RequestParam("mascota") String mascotaJson,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        // Convertir el string JSON a un objeto Mascota
        Mascota mascota = objectMapper.readValue(mascotaJson, Mascota.class);

        // Asociar al propietario
        Propietario propietario = propietarioRepository.findById(mascota.getPropietario().getId())
                .orElseThrow(() -> new EntityNotFoundException("Propietario no encontrado"));
        mascota.setPropietario(propietario);

        // Si se subió un archivo, guardarlo y establecer la URL
        if (file != null && !file.isEmpty()) {
            String fileUrl = fileStorageService.save(file);
            mascota.setImageUrl(fileUrl);
        }

        Mascota mascotaGuardada = mascotaService.guardar(mascota);
        return ResponseEntity.status(HttpStatus.CREATED).body(mascotaGuardada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> actualizarMascota(
            @PathVariable Long id,
            @RequestParam("mascota") String mascotaJson,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        Mascota datosActualizados = objectMapper.readValue(mascotaJson, Mascota.class);
        Mascota mascotaExistente = mascotaService.findMascotaById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mascota no encontrada con ID: " + id));

        // --- CORRECCIÓN AQUÍ ---
        // Actualizar todos los campos necesarios del objeto existente con los datos nuevos
        mascotaExistente.setNombre(datosActualizados.getNombre());
        mascotaExistente.setEspecie(datosActualizados.getEspecie());
        mascotaExistente.setRaza(datosActualizados.getRaza()); // <-- AÑADIR ESTA LÍNEA
        mascotaExistente.setEdad(datosActualizados.getEdad());   // <-- AÑADIR ESTA LÍNEA
        // ... (puedes añadir otros campos como peso, color, etc. si lo deseas)

        if (file != null && !file.isEmpty()) {
            String fileUrl = fileStorageService.save(file);
            mascotaExistente.setImageUrl(fileUrl);
        }

        Mascota mascotaActualizada = mascotaService.guardar(mascotaExistente);
        return ResponseEntity.ok(mascotaActualizada);
    }

    // --- MÉTODOS GET Y DELETE (SIN CAMBIOS) ---
    @GetMapping
    public List<Mascota> findAll() { return mascotaService.findAllMascotas(); }

    @GetMapping("/propietario/{propietarioId}")
    public ResponseEntity<List<Mascota>> findByPropietarioId(@PathVariable Long propietarioId) {
        List<Mascota> mascotas = mascotaService.findByPropietarioId(propietarioId);
        if (mascotas.isEmpty()) { return ResponseEntity.noContent().build(); }
        return ResponseEntity.ok(mascotas);
    }

    @GetMapping("/{id}")
    public Optional<Mascota> findMascotaById(@PathVariable Long id) { return mascotaService.findMascotaById(id); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarMascota(@PathVariable Long id) {
        mascotaService.eliminar(id);
        return ResponseEntity.ok(Map.of("eliminado", true));
    }
}