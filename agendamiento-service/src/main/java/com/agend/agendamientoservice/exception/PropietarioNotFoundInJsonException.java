package com.agend.agendamientoservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Esto enviará un error 400 (Solicitud incorrecta) al frontend
public class PropietarioNotFoundInJsonException extends RuntimeException {
    public PropietarioNotFoundInJsonException(String message) {
        super(message);
    }
}