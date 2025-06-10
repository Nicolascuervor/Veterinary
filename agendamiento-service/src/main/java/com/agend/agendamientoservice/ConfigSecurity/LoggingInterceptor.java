package com.agend.agendamientoservice.ConfigSecurity;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        System.out.println("📡 Realizando solicitud a: " + request.getURI());
        System.out.println("📦 Headers:");
        request.getHeaders().forEach((k, v) -> System.out.println(" - " + k + ": " + v));
        return execution.execute(request, body);
    }
}
