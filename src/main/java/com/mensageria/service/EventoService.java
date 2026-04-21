package com.mensageria.service;

import com.mensageria.dto.AtualizaResultadoRequest;
import com.mensageria.dto.EventoResponse;
import com.mensageria.dto.NovoEventoRequest;
import com.mensageria.entity.Evento;
import com.mensageria.exception.EventoNotFoundException;
import com.mensageria.repository.EventoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service // Registra como bean: pode ser injetado no Controller
public class EventoService {
    private final EventoRepository repository;

    public EventoService(EventoRepository repository) {
        this.repository = repository;
    }

    /**
     * Transação de escrita: se algo falhar, o banco desfaz o insert.
     */
    @Transactional
    public EventoResponse criarEvento(NovoEventoRequest request) {
        Evento evento = new Evento(request.payload());
        evento = repository.save(evento);
        return EventoResponse.from(evento);
    }

    /**
     * Somente leitura: otimização hint para o Hibernate.
     */
    @Transactional(readOnly = true)
    public List<EventoResponse> listarPendentes() {
        return repository.findByStatusOrderByCriadoEmAsc(Evento.StatusEvento.PENDENTE).map(EventoResponse::from)
                .toList();
    }

    /**
     * Pega o primeiro pendente (FIFO), marca como PROCESSANDO, salva.
     */
    @Transactional
    public EventoResponse obterProximoEvento() {
        List<Evento> pendentes = repository.findPendentesOrdenadosPorCriacao(
                Evento.StatusEvento.PENDENTE,
                PageRequest.of(0, 1) // página 0, tamanho 1
        );
        if (pendentes.isEmpty()) {
            return null; // Controller pode traduzir para HTTP 204 No Content
        }
        Evento evento = pendentes.get(0);
        evento.setStatus(Evento.StatusEvento.PROCESSANDO);
        evento = repository.save(evento);
        return EventoResponse.from(evento);
    }

    @Transactional
    public EventoResponse atualizarResultado(Long id, AtualizaResultadoRequest request) {
        // findById retorna Optional: orElseThrow evita if (null) espalhado
        Evento evento = repository.findById(id)
                .orElseThrow(() -> new EventoNotFoundException(id));
        evento.setStatus(request.status());
        evento.setResultado(request.resultado());
        evento.setProcessadoEm(Instant.now());
        evento = repository.save(evento);
        return EventoResponse.from(evento);
    }
}