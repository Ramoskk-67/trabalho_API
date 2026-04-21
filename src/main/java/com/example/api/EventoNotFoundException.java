package com.example.api;

public class EventoNotFoundException extends RuntimeException {
    public EventoNotFoundException(Long id) {
        super("Evento não encontrado: " + id);
    }
}
