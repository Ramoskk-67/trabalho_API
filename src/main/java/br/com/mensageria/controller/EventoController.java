package br.com.mensageria.controller;

import br.com.mensageria.dto.AtualizaResultadoRequest;
import br.com.mensageria.dto.EventoResponse;
import br.com.mensageria.dto.NovoEventoRequest;
import br.com.mensageria.entity.Evento;
import br.com.mensageria.service.EventoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @PostMapping
    public ResponseEntity<EventoResponse> criarEvento(@Valid @RequestBody NovoEventoRequest request) {
        EventoResponse response = eventoService.criarEvento(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<EventoResponse>> listarPendentes() {
        return ResponseEntity.ok(eventoService.listarPendentes());
    }

    // 🔥 NOVO - LISTAR TODOS
    @GetMapping
    public ResponseEntity<List<EventoResponse>> listarTodos() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

    // 🔥 NOVO - FILTRAR POR STATUS
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventoResponse>> listarPorStatus(@PathVariable String status) {
        Evento.StatusEvento statusEnum = Evento.StatusEvento.valueOf(status.toUpperCase());
        return ResponseEntity.ok(eventoService.listarPorStatus(statusEnum));
    }

    @GetMapping("/primeiro")
    public ResponseEntity<EventoResponse> obterPrimeiroEvento() {
        EventoResponse response = eventoService.obterProximoEvento();
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/resultado")
    public ResponseEntity<EventoResponse> atualizarResultado(
            @PathVariable Long id,
            @Valid @RequestBody AtualizaResultadoRequest request) {
        return ResponseEntity.ok(eventoService.atualizarResultado(id, request));
    }
    
}