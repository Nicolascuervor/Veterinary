package com.agend.agendamientoservice.ConfigSecurity;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Obtenemos la ruta absoluta a la carpeta 'uploads'
        String uploadPath = Paths.get("uploads").toFile().getAbsolutePath();

        // Mapeamos la URL '/uploads/**' para que sirva archivos desde el directorio 'uploads/'
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/" + uploadPath + "/");
    }
}