package br.com.example.worker;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    
    private static final String STATUS_CONCLUIDO = "CONCLUIDO";
    
    //  Configuração para SIMULAR erros
    private static final boolean SIMULAR_ERROS = true;
    private static final double TAXA_ERRO = 0.5;
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        String baseUrl = System.getenv("API_BASE_URL") != null
            ? System.getenv("API_BASE_URL")
            : "http://localhost:8080";

        System.out.println("=== Worker de Notificação ===");
        System.out.println("API: " + baseUrl);
        System.out.println("====================================================");

        while (true) {
            try {
                executarCiclo(baseUrl);
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void executarCiclo(String baseUrl) throws Exception {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        // 1. BUSCAR EVENTO
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/eventos/primeiro"))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("📭 Nenhum evento disponível");
            return;
        }

        String corpo = response.body();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(corpo);

        long id = node.get("id").asLong();
        JsonNode payload = node.get("payload");

        String usuarioId = payload.has("usuarioId") ? payload.get("usuarioId").asText() : "desconhecido";
        String produtoId = payload.has("produtoId") ? payload.get("produtoId").asText() : "desconhecido";

        // 🔥 AQUI AGORA MOSTRA O PROCESSANDO
        System.out.println("\n🔄 Evento " + id + " está em PROCESSANDO");
        System.out.println("📦 Usuário: " + usuarioId + " | Produto: " + produtoId);

        // 🔥 Delay só pra visualização (importante pra apresentação)
        Thread.sleep(2000);

        // 2. PROCESSAR
        boolean sucesso = processar();

        // 3. DEFINIR RESULTADO
        String status;
        String resultado;

        if (sucesso) {
            status = STATUS_CONCLUIDO;
            resultado = "OK";
        } else {
<<<<<<< HEAD
            // 🔁 VOLTA PRA FILA (retry automático)
=======
            
>>>>>>> b225ee2614fc1fbdfdeef28540f967d49d04b782
            status = "PENDENTE";
            resultado = "Erro - será reprocessado";
        }

        String json = "{\"status\":\"" + status + "\",\"resultado\":\"" + resultado + "\"}";

        HttpRequest patch = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/eventos/" + id + "/resultado"))
            .header("Content-Type", "application/json")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
            .build();

        client.send(patch, HttpResponse.BodyHandlers.ofString());

        System.out.println("➡️ Status atualizado: " + status);
    }

    private static boolean processar() {
        try {
            Thread.sleep(2000);

            if (SIMULAR_ERROS && random.nextDouble() < TAXA_ERRO) {
                System.out.println(" Falha simulada ");
                return false;
            }

            System.out.println(" Sucesso");
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}