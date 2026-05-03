package br.com.mensageria.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record NovoEventoRequest(
    @NotNull(message = "Payload é obrigatório")
    Map<String, Object> payload
) {}
