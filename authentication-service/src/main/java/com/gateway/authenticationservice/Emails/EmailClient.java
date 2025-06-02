package com.gateway.authenticationservice.Emails;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EmailClient {

    private final RestTemplate restTemplate;

    @Value("${email.service.url}")
    private String emailServiceUrl;

    public EmailClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void enviarEmail(String[] destinatarios, String asunto, String mensaje) {
        EmailDTO email = new EmailDTO(destinatarios, asunto, mensaje);

        try {
            restTemplate.postForEntity(emailServiceUrl + "/v1/sendMessage", email, Void.class);
            System.out.println("📨 Email enviado a: " + String.join(",", destinatarios));
        } catch (Exception e) {
            System.err.println("❌ Error al enviar email: " + e.getMessage());
        }
    }

    public void sendEmailTemplate(String[] toUser, String subject, String templateName, Map<String, Object> model) {
        EmailTemplateDTO templateDTO = new EmailTemplateDTO(toUser, subject, templateName, model);

        try {
            restTemplate.postForEntity(emailServiceUrl + "/v1/send-html", templateDTO, Void.class);
            System.out.println("✅ Email con plantilla enviado a: " + String.join(",", toUser));
        } catch (Exception e) {
            System.err.println("❌ Error al enviar email con plantilla: " + e.getMessage());
        }
    }
}
