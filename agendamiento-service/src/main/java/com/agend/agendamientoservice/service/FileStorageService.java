// Reemplaza TODO el contenido de tu archivo con este código robusto

package com.agend.agendamientoservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final Path uploadDir;

    // Usamos el constructor para inicializar la ruta y crear el directorio.
    // Esto es más seguro que usar @Value en un campo.
    // --- CORRECCIÓN CLAVE AQUÍ ---
    // Asegúrate de que el constructor también use "${app.upload.dir}"
    public FileStorageService(@Value("${app.upload.dir}") String uploadPath) {
        this.uploadDir = Paths.get(uploadPath);
        try {
            Files.createDirectories(this.uploadDir);
            logger.info("Directorio de carga de imágenes asegurado en: {}", this.uploadDir.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de carga de imágenes.", e);
        }
    }

    public String save(MultipartFile file) {
        // Limpiamos el nombre del archivo de cualquier caracter malicioso (ej: ../)
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Generamos un nombre de archivo único para evitar colisiones.
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        // Resolvemos la ruta completa del archivo de destino de forma segura.
        Path targetLocation = this.uploadDir.resolve(uniqueFilename);

        try (InputStream inputStream = file.getInputStream()) {
            // Copiamos el archivo al destino, reemplazándolo si ya existe.
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Archivo guardado exitosamente en: {}", targetLocation.toAbsolutePath());
        } catch (IOException ex) {
            logger.error("Error al guardar el archivo {} en {}", uniqueFilename, targetLocation, ex);
            // Lanzamos una excepción con un mensaje claro.
            throw new RuntimeException("Error al guardar el archivo: " + uniqueFilename, ex);
        }

        // Devolvemos la RUTA WEB que el frontend usará.
        // Debe coincidir con tu ResourceHandler en MvcConfig.
        // Aseguramos que siempre use slashes (/) para la URL web.
        return "/imagenes/mascotas/" + uniqueFilename;

    }
}