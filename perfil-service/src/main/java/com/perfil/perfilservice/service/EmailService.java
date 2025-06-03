package com.perfil.perfilservice.service;

import com.perfil.perfilservice.model.PerfilUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    // Simulación del servicio de email - deberías integrarlo con tu email-service
    public void enviarEmailBienvenidaPerfil(PerfilUsuario perfil) {
        // Aquí implementarías la llamada a tu email-service
        System.out.println("📧 Enviando email de bienvenida a: " + perfil.getEmail());
        System.out.println("👋 Hola " + perfil.getNombreCompleto() + ", tu perfil ha sido creado exitosamente.");
    }

    public void enviarNotificacionCambioPerfil(PerfilUsuario perfil, String tipoCambio) {
        System.out.println("📧 Enviando notificación de cambio de perfil a: " + perfil.getEmail());
        System.out.println("🔄 Tipo de cambio: " + tipoCambio);
    }
}
