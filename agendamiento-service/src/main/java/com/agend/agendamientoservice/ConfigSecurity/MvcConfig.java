package com.agend.agendamientoservice.ConfigSecurity;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(MvcConfig.class);

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Obtiene la ruta absoluta de la carpeta de subidas
        String absoluteUploadPath = Paths.get(uploadDir).toFile().getAbsolutePath();


        registry.addResourceHandler("/imagenes/mascotas/**")
                .addResourceLocations("file:/" + absoluteUploadPath + "/");
    }
}