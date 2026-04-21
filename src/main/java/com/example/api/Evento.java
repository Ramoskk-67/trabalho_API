package br.com.mensageria.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private JsonNode payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEvento status = StatusEvento.PENDENTE;

    @Column(length = 1000)
    private String resultado;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "processado_em")
    private Instant processadoEm;

    public Evento() {}

    public Evento(JsonNode payload) {
        this.payload = payload;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public JsonNode getPayload() { return payload; }
    public void setPayload(JsonNode payload) { this.payload = payload; }

    public StatusEvento getStatus() { return status; }
    public void setStatus(StatusEvento status) { this.status = status; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }

    public Instant getProcessadoEm() { return processadoEm; }
    public void setProcessadoEm(Instant processadoEm) { this.processadoEm = processadoEm; }

    public enum StatusEvento {
        PENDENTE,
        PROCESSANDO,
        CONCLUIDO,
        FALHOU
    }
}