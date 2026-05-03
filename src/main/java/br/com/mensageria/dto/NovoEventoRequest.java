package br.com.mensageria.dto;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotNull;

public record NovoEventoRequest(
    @NotNull(message = "Payload é obrigatório")
    ObjectNode payload
) {}
