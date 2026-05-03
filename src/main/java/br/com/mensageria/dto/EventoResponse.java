package br.com.mensageria.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import br.com.mensageria.entity.Evento;

import java.time.Instant;
import java.util.Map;

/**
 * Resposta da API: pode espelhar a entity, mas só com o que você quer expor.
 * Retornamos o payload como Map para evitar a serialização de detalhes internos de JsonNode.
 */
public record EventoResponse(
        Long id,
        Map<String, Object> payload,
        Evento.StatusEvento status,
        String resultado,
        Instant criadoEm,
        Instant processadoEm) {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Fábrica: converte Entity (persistência) → DTO (API). */
    public static EventoResponse from(Evento evento) {
        JsonNode payloadNode = evento.getPayload();
        Map<String, Object> payloadMap = payloadNode != null
                ? MAPPER.convertValue(payloadNode, new TypeReference<>() {})
                : Map.of();

        return new EventoResponse(
                evento.getId(),
                payloadMap,
                evento.getStatus(),
                evento.getResultado(),
                evento.getCriadoEm(),
                evento.getProcessadoEm());
    }
}
