package br.com.mensageria.controller;
import br.com.mensageria.dto.AtualizaResultadoRequest;
import br.com.mensageria.dto.EventoResponse;
import br.com.mensageria.dto.NovoEventoRequest;
import br.com.mensageria.service.EventoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * @RequestMapping prefixa todas as URLs desta classe.
 * @Valid dispara a validação dos DTOs (ex.: @NotNull).
 */
@RestController
@RequestMapping("/eventos")
public class EventoController {
    private final EventoService eventoService;
    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }
    @PostMapping
    public ResponseEntity<EventoResponse> criarEvento(@Valid @RequestBody NovoEventoReq
        EventoResponse response = eventoService.criarEvento(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/pendentes")
    public ResponseEntity<List<EventoResponse>> listarPendentes() {
        return ResponseEntity.ok(eventoService.listarPendentes());
    }
    @GetMapping("/primeiro")
    public ResponseEntity<EventoResponse> obterPrimeiroEvento() {
        EventoResponse response = eventoService.obterProximoEvento();
        if (response == null) {
            return ResponseEntity.noContent().build(); // 204
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