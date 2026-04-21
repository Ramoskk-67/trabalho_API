package com.mensageria.dto;
import com.mensageria.entity.Evento;
import jakarta.validation.constraints.NotNull;
/**
* PATCH típico: só os campos que o cliente pode mudar nesta operação.
*/
public record AtualizaResultadoRequest(
@NotNull(message = "Status é obrigatório")
Evento.StatusEvento status,
String resultado // opcional: ex. mensagem de erro ou sucesso
) {}
