package com.mensageria.dto;

import com.mensageria.entity.Evento;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

/**
 * Resposta da API: pode espelhar a entity, mas só com o que você quer expor.
 * Se a entity usa JsonNode no payload, o response pode usar JsonNode também —
 * o Spring serializa para JSON automaticamente.
 */
public record EventoResponse(
        Long id,
        JsonNode payload,
        Evento.StatusEvento status,
        String resultado,
        Instant criadoEm,
        Instant processadoEm) {
    /** Fábrica: converte Entity (persistência) → DTO (API). */
    public static EventoResponse from(Evento evento) {
        return new EventoResponse(
                evento.getId(),
                evento.getPayload(),
                evento.getStatus(),
                evento.getResultado(),
                evento.getCriadoEm(),
                evento.getProcessadoEm());
    }
}
