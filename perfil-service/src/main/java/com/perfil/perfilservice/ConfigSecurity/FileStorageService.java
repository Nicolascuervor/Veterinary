package com.perfil.perfilservice.ConfigSecurity;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@AllArgsConstructor

public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService() {
        // Normalizar y crear la ruta de subida
        this.fileStorageLocation = Paths.get("C:/veterinaria_uploads/profile-images").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio para almacenar las imágenes de perfil.", ex);
        }
    }
    public String storeFile(MultipartFile file, Long userId) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        try {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } catch (Exception e) {
            fileExtension = "";
        }
        // Crear un nombre de archivo único para evitar colisiones
        String fileName = "user-" + userId + "-" + System.currentTimeMillis() + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo almacenar el archivo " + fileName + ". Por favor, inténtelo de nuevo.", ex);
        }
    }
}