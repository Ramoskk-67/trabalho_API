package br.com.mensageria.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;


@NotNull
 
public record NovoEventoRequest(

    @NotNull(message = "Payload é obrigatório")
    JsonNode payload

) {}
