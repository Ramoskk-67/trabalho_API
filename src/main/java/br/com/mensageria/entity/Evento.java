package br.com.mensageria.entity;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
/**
 * Representa a tabela "eventos".
 * Cada linha = um evento na fila (mensageria simulada).
 */
@Entity
@Table(name = "eventos")
public class Evento {
    // Chave numérica gerada pelo banco (SERIAL / IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Payload em JSON de verdade no PostgreSQL (tipo jsonb).
     * @JdbcTypeCode(SqlTypes.JSON) diz ao Hibernate como conversar com o driver JDBC.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private JsonNode payload;
    // Enum armazenado como texto na coluna (PENDENTE, PROCESSANDO, ...)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEvento status = StatusEvento.PENDENTE;
    @Column(length = 1000)
    private String resultado;
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();
    @Column(name = "processado_em")
    private Instant processadoEm;
    public Evento() {
        // Construtor vazio exigido pelo JPA para instanciar a entidade via reflexão
    }
    public Evento(JsonNode payload) {
        this.payload = payload;
    }

    // Novo construtor para aceitar String JSON
    public Evento(String payloadJson) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            this.payload = mapper.readTree(payloadJson);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter String para JsonNode", e);
        }
    }
    // --- getters e setters (JPA precisa acessar os campos) --
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
    /**
     * Estados possíveis do processamento.
     * Pode ficar aqui dentro da entity ou em arquivo próprio — questão de organização.
     */
    public enum StatusEvento {
        PENDENTE,
        PROCESSANDO,
        CONCLUIDO,
        FALHOU
    }
}
