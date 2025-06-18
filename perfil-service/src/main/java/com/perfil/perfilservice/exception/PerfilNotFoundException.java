package com.perfil.perfilservice.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Con esta anotación, Spring devolverá automáticamente un 404 Not Found si esta excepción no es capturada.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PerfilNotFoundException extends RuntimeException { // <<== CAMBIO 1: Hereda de RuntimeException
    public PerfilNotFoundException(String message) {
        super(message); // <<== CAMBIO 2: Llama al constructor de la clase padre para pasar el mensaje.
    }
}