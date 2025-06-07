package com.agend.agendamientoservice.controller;


import com.agend.agendamientoservice.model.Propietario;
import com.agend.agendamientoservice.repository.PropietarioRepository;
import com.agend.agendamientoservice.service.PropietarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/propietarios")
public class PropietarioController {
    @Autowired
    private PropietarioService propietarioService;

    @Autowired
    private PropietarioRepository propietarioRepository;

    @GetMapping
    public List<Propietario> findAll() {
        return propietarioService.findAllPropietarios();
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> crearPropietario(@RequestBody PropietarioRequest request) {
        Propietario p = new Propietario();
        p.setNombre(request.nombre);
        p.setApellido(request.apellido);
        p.setTelefono(request.telefono);
        p.setDireccion(request.direccion);
        p.setDni(request.cedula);
        p.setUsuarioId(request.usuarioId);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());

        propietarioRepository.save(p);
        return ResponseEntity.ok("Propietario creado");
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

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Propietario> getByUsuarioId(@PathVariable Long usuarioId) {
        Optional<Propietario> p = propietarioRepository.findByUsuarioId(usuarioId);
        return p.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/debug/headers")
    public ResponseEntity<Map<String, String>> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = Collections.list(request.getHeaderNames())
                .stream().collect(Collectors.toMap(h -> h, request::getHeader));
        return ResponseEntity.ok(headers);
    }


}
