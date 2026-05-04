package br.com.mensageria.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.mensageria.dto.AtualizaResultadoRequest;
import br.com.mensageria.dto.EventoResponse;
import br.com.mensageria.dto.NovoEventoRequest;
import br.com.mensageria.entity.Evento;
import br.com.mensageria.exception.EventoNotFoundException;
import br.com.mensageria.repository.EventoRepository;

import java.time.Instant;
import java.util.List;

@Service
public class EventoService {

    private final EventoRepository repository;

    public EventoService(EventoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EventoResponse criarEvento(NovoEventoRequest request) {
        Evento evento = new Evento(request.payload());
        evento = repository.save(evento);
        return EventoResponse.from(evento);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarPendentes() {
        return repository.findByStatusOrderByCriadoEmAsc(Evento.StatusEvento.PENDENTE)
                .stream()
                .map(EventoResponse::from)
                .toList();
    }

    // 🔥 NOVO - LISTAR TODOS
    @Transactional(readOnly = true)
    public List<EventoResponse> listarTodos() {
        return repository.findAll()
                .stream()
                .map(EventoResponse::from)
                .toList();
    }

    // 🔥 NOVO - LISTAR POR STATUS
    @Transactional(readOnly = true)
    public List<EventoResponse> listarPorStatus(Evento.StatusEvento status) {
        return repository.findByStatusOrderByCriadoEmAsc(status)
                .stream()
                .map(EventoResponse::from)
                .toList();
    }

    @Transactional
    public EventoResponse obterProximoEvento() {
        List<Evento> pendentes = repository.findPendentesOrdenadosPorCriacao(
                Evento.StatusEvento.PENDENTE,
                PageRequest.of(0, 1)
        );

        if (pendentes.isEmpty()) {
            return null;
        }

        Evento evento = pendentes.get(0);
        evento.setStatus(Evento.StatusEvento.PROCESSANDO);
        evento = repository.save(evento);

        return EventoResponse.from(evento);
    }

    @Transactional
    public EventoResponse atualizarResultado(Long id, AtualizaResultadoRequest request) {
        Evento evento = repository.findById(id)
                .orElseThrow(() -> new EventoNotFoundException(id));

        evento.setStatus(request.status());
        evento.setResultado(request.resultado());
        evento.setProcessadoEm(Instant.now());

        evento = repository.save(evento);

        return EventoResponse.from(evento);
    }
    
}