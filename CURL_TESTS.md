# Testes com cURL - API Mensageria

## Pré-requisitos
```bash
docker-compose up --build
```

## Endpoints de Teste

### 1. Criar novo evento (POST)
```bash
curl -s -X POST http://localhost:8080/eventos \
  -H "Content-Type: application/json" \
  -d '{"payload":{"tipo":"teste","valor":1}}'
```

**Resposta esperada (201 Created):**
```json
{
  "id": 1,
  "payload": {"tipo":"teste","valor":1},
  "status": "PENDENTE",
  "resultado": null,
  "criadoEm": "2026-05-01T...",
  "processadoEm": null
}
```

---

### 2. Listar eventos pendentes (GET)
```bash
curl -s http://localhost:8080/eventos/pendentes
```

**Resposta esperada (200 OK):**
```json
[
  {
    "id": 1,
    "payload": {"tipo":"teste","valor":1},
    "status": "PENDENTE",
    ...
  }
]
```

---

### 3. Obter primeiro evento pendente (marca como PROCESSANDO)
```bash
curl -s http://localhost:8080/eventos/primeiro
```

**Resposta esperada (200 OK):**
```json
{
  "id": 1,
  "payload": {"tipo":"teste","valor":1},
  "status": "PROCESSANDO",
  ...
}
```

**Se não houver pendentes (204 No Content):**
```bash
# Retorna vazio
```

---

### 4. Atualizar resultado do evento (PATCH)
```bash
curl -s -X PATCH http://localhost:8080/eventos/1/resultado \
  -H "Content-Type: application/json" \
  -d '{"status":"CONCLUIDO","resultado":"ok"}'
```

**Resposta esperada (200 OK):**
```json
{
  "id": 1,
  "payload": {"tipo":"teste","valor":1},
  "status": "CONCLUIDO",
  "resultado": "ok",
  "processadoEm": "2026-05-01T..."
}
```

---

## Fluxo Completo de Teste

```bash
# 1. Criar evento
curl -s -X POST http://localhost:8080/eventos \
  -H "Content-Type: application/json" \
  -d '{"payload":{"tipo":"pedido","id_pedido":12345,"cliente":"João"}}'

# 2. Listar pendentes
curl -s http://localhost:8080/eventos/pendentes | jq

# 3. Pegar primeiro (muda status para PROCESSANDO)
curl -s http://localhost:8080/eventos/primeiro | jq

# 4. Atualizar resultado
curl -s -X PATCH http://localhost:8080/eventos/1/resultado \
  -H "Content-Type: application/json" \
  -d '{"status":"CONCLUIDO","resultado":"Processado com sucesso"}'

# 5. Verificar resultado
curl -s http://localhost:8080/eventos/pendentes | jq
```

---

## Conexão ao PostgreSQL

```bash
# Acessar psql dentro do container
docker exec -it mensageria-postgres psql -U postgres -d mensageria_db

# Listar eventos (JSONB)
SELECT id, payload, status, resultado, criado_em FROM eventos;

# Buscar por conteúdo JSON
SELECT * FROM eventos WHERE payload @> '{"tipo":"teste"}';
```

---

## Notas

- **JSONB nativo**: A coluna `payload` é armazenada como `jsonb` no PostgreSQL
- **Status**: PENDENTE → PROCESSANDO → CONCLUIDO / FALHOU
- **Healthcheck**: API aguarda PostgreSQL estar saudável antes de iniciar
