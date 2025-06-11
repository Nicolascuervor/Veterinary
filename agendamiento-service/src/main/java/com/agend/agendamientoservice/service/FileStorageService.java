package com.agend.agendamientoservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    // La ruta donde se guardan los archivos físicamente sigue igual
    private final Path root = Paths.get("uploads/pet-images");

    // ... (método constructor)

    public String save(MultipartFile file) {
        try {
            // ... (lógica para guardar el archivo) ...
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.root.resolve(filename));

            // --- CORRECCIÓN AQUÍ ---
            // Devolvemos la ruta web, no la del sistema de archivos.
            // Esta ruta coincide con el handler que creamos en MvcConfig.
            return "/uploads/pet-images/" + filename;

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage());
        }
    }
}