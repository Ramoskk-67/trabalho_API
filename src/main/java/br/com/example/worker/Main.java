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
    private static final String STATUS_FALHOU = "FALHOU";
    
    // 🔧 Configuração para SIMULAR erros (você muda aqui)
    private static final boolean SIMULAR_ERROS = false;     // Liga/desliga simulação
    private static final double TAXA_ERRO = 0.5;            // 50% de chance de erro
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        String baseUrl = System.getenv("API_BASE_URL") != null
            ? System.getenv("API_BASE_URL")
            : "http://localhost:8080";

        String queueName = System.getenv("QUEUE_NAME") != null
            ? System.getenv("QUEUE_NAME")
            : "notificacao";

        System.out.println("=== Worker de Notificação (com simulação de erro) ===");
        System.out.println("API: " + baseUrl);
        System.out.println("Fila: " + queueName);
        if (SIMULAR_ERROS) {
            System.out.println("⚠️ SIMULAÇÃO DE ERRO ATIVADA: " + (TAXA_ERRO * 100) + "% de chance de falha");
        }
        System.out.println("====================================================");

        while (true) {
            try {
                executarCiclo(baseUrl);
                Thread.sleep(10000); // espera 10s
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("❌ Execução interrompida: " + e.getMessage());
                break;
            } catch (Exception e) {
                System.err.println("❌ Erro de comunicação: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void executarCiclo(String baseUrl) throws Exception {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        // 1. BUSCAR EVENTO
        String urlBusca = baseUrl + "/eventos/primeiro";
        System.out.println("\n🔍 Buscando próximo evento em: " + urlBusca);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlBusca))
            .timeout(Duration.ofSeconds(20))
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        System.out.println("Status code: " + statusCode);

        // Fila vazia
        if (statusCode == 204 || statusCode == 404) {
            System.out.println("📭 Fila vazia. Aguardando próximo ciclo.");
            return;
        }

        if (statusCode != 200) {
            System.err.println("❌ Erro inesperado da API: " + statusCode);
            return;
        }

        String corpo = response.body();
        System.out.println("📦 Evento recebido: " + corpo);

        // Usar Jackson para parsear o JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(corpo);

        long id = node.get("id").asLong();
        System.out.println("   ID do evento: " + id);

        JsonNode payloadNode = node.get("payload");
        System.out.println("   Payload: " + payloadNode.toString());

        // Corrigir nomes dos campos para usuarioId e produtoId
        String usuarioId = payloadNode.has("usuarioId") ? payloadNode.get("usuarioId").asText() : "desconhecido";
        String produtoId = payloadNode.has("produtoId") ? payloadNode.get("produtoId").asText() : "desconhecido";
        String tipo = payloadNode.has("tipo") ? payloadNode.get("tipo").asText() : "generico";

        System.out.println("   Usuário ID: " + usuarioId);
        System.out.println("   Produto ID: " + produtoId);
        System.out.println("   Tipo: " + tipo);

        // Tratar tipos diferentes de evento
        switch (tipo) {
            case "sms_confirmacao":
                System.out.println("📲 Enviando código de confirmação para telefone: " + payloadNode.path("telefone").asText("(não informado)"));
                break;
            case "sms_agradecimento":
                System.out.println("🙏 Enviando mensagem de agradecimento para telefone: " + payloadNode.path("telefone").asText("(não informado)"));
                break;
            default:
                System.out.println("⚠️ Tipo desconhecido: " + tipo);
        }

        // 2. PROCESSAR COM SIMULAÇÃO DE ERRO
        boolean sucesso = processarEventoComSimulacao(usuarioId, produtoId);

        // 3. ATUALIZAR RESULTADO
        String status = sucesso ? STATUS_CONCLUIDO : STATUS_FALHOU;
        String resultado = sucesso
            ? "Notificação processada com sucesso"
            : "FALHA SIMULADA: Erro ao processar notificação";

        String corpoJson = "{\"status\":\"" + status + "\",\"resultado\":\"" + resultado + "\"}";

        String urlPatch = baseUrl + "/eventos/" + id + "/resultado";
        System.out.println("\n📤 Atualizando resultado em: " + urlPatch);
        System.out.println("   Body: " + corpoJson);

        HttpRequest patchRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlPatch))
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/json")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(corpoJson))
            .build();

        HttpResponse<String> patchResponse = client.send(patchRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("   PATCH status: " + patchResponse.statusCode());

        if (patchResponse.statusCode() == 200) {
            if (sucesso) {
                System.out.println("   ✅ Processamento concluído com SUCESSO!");
            } else {
                System.out.println("   ❌ Processamento FALHOU (simulado)!");
            }
        } else {
            System.err.println("   ❌ Erro ao atualizar status: " + patchResponse.body());
        }
    }
    
    /**
     * Processa o evento com SIMULAÇÃO DE ERRO
     * 
     * @return true se processou com sucesso, false se houve erro simulado
     */
    private static boolean processarEventoComSimulacao(String usuarioId, String produtoId) {
        System.out.println("\n📧 PROCESSANDO NOTIFICAÇÃO...");
        
        try {
            // Simula processamento (demora 2 segundos)
            Thread.sleep(2000);
            
            // 🔴 SIMULAÇÃO DE ERRO (se ativada)
            if (SIMULAR_ERROS) {
                double valorAleatorio = random.nextDouble();
                if (valorAleatorio < TAXA_ERRO) {
                    System.err.println("   🔴 ERRO SIMULADO: Falha no processamento!");
                    System.err.println("   Motivo: Erro aleatório (taxa de " + (TAXA_ERRO * 100) + "%)");
                    return false;
                }
            }
            
            // Sucesso!
            System.out.println("   ✅ NOTIFICAÇÃO PROCESSADA COM SUCESSO!");
            System.out.println("   Registrado interesse do usuário: " + usuarioId);
            System.out.println("   Produto: " + produtoId);
            return true;
            
        } catch (InterruptedException e) {
            System.err.println("   ❌ Processamento interrompido");
            return false;
        } catch (Exception e) {
            System.err.println("   ❌ Erro no processamento: " + e.getMessage());
            return false;
        }
    }
    
    // As funções extrairPayload e extrairValor foram removidas pois agora usamos Jackson para parsear JSON
}