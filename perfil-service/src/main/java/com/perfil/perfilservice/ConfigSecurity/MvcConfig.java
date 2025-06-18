package com.perfil.perfilservice.ConfigSecurity;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // La ruta URL que el frontend usará para pedir las imágenes.
        String resourcePath = "/uploads/profile-images/**";

        // La ubicación física en el disco duro donde están guardadas las imágenes.
        // Debe coincidir con la ruta en tu FileStorageService.
        String resourceLocation = "file:./C:/veterinaria_uploads/profile-images/";

        registry.addResourceHandler(resourcePath)
                .addResourceLocations(resourceLocation);
    }
}