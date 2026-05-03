package br.com.example.worker;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    
    private static final String STATUS_CONCLUIDO = "CONCLUIDO";
    private static final String STATUS_FALHOU = "FALHOU";
    
    // 🔧 Configuração para SIMULAR erros (você muda aqui)
    private static final boolean SIMULAR_ERROS = true;      // Liga/desliga simulação
    private static final double TAXA_ERRO = 0.5;            // 50% de chance de erro
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        String baseUrl = System.getenv("API_BASE_URL") != null
            ? System.getenv("API_BASE_URL")
            : "http://localhost:8080";
        
        String queueName = System.getenv("QUEUE_NAME") != null
            ? System.getenv("QUEUE_NAME")
            : "notificacao";
        
        try {
            
            System.out.println("=== Worker de Notificação (com simulação de erro) ===");
            System.out.println("API: " + baseUrl);
            System.out.println("Fila: " + queueName);
            if (SIMULAR_ERROS) {
                System.out.println("⚠️ SIMULAÇÃO DE ERRO ATIVADA: " + (TAXA_ERRO * 100) + "% de chance de falha");
            }
            System.out.println("====================================================");
            
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
            
            // ✅ 1. BUSCAR EVENTO
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
            
            // Extrai ID
            Pattern pattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
            Matcher matcher = pattern.matcher(corpo);
            if (!matcher.find()) {
                System.err.println("❌ ID não encontrado na resposta");
                return;
            }
            long id = Long.parseLong(matcher.group(1));
            System.out.println("   ID do evento: " + id);
            
            // Extrai payload
            String payload = extrairPayload(corpo);
            System.out.println("   Payload: " + payload);
            
            String usuarioId = extrairValor(payload, "usuario_id");
            String produtoId = extrairValor(payload, "produto_id");
            String acao = extrairValor(payload, "acao");

            System.out.println("   Usuário ID: " + usuarioId);
            System.out.println("   Produto ID: " + produtoId);
            if (acao == null || acao.equals("desconhecido")) {
                System.err.println("⚠️  Campo 'acao' não encontrado no payload! Valor recebido: '" + acao + "'");
                // Aqui você pode decidir se quer abortar, lançar exceção ou seguir com valor padrão
            } else {
                System.out.println("   Ação: " + acao);
            }
            
            // ✅ 2. PROCESSAR COM SIMULAÇÃO DE ERRO
            boolean sucesso = processarEventoComSimulacao(usuarioId, produtoId);
            
            // ✅ 3. ATUALIZAR RESULTADO
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
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ Execução interrompida: " + e.getMessage());
        } catch (java.net.ConnectException e) {
            System.err.println("❌ ERRO DE CONEXÃO: Não foi possível conectar à API!");
            System.err.println("   Verifique se a API está rodando em: " + baseUrl);
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("❌ TIMEOUT: A API demorou muito para responder!");
        } catch (Exception e) {
            System.err.println("❌ Erro de comunicação: " + e.getMessage());
            e.printStackTrace();
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
    
    /**
     * Extrai o payload do JSON do evento
     */
    private static String extrairPayload(String json) {
        Pattern pattern = Pattern.compile("\"payload\"\\s*:\\s*(\\{.+?\\})(?=,\\s*\"|\\s*\\})");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        pattern = Pattern.compile("\"payload\"\\s*:\\s*\"(\\{.+?\\})\"");
        matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).replace("\\\"", "\"");
        }
        
        return "{}";
    }
    
    /**
     * Extrai um valor de um JSON usando regex
     */
    private static String extrairValor(String json, String chave) {
        if (json == null || json.isEmpty()) {
            return "desconhecido";
        }
        
        String patternStr = "\"" + chave + "\"\\s*:\\s*\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(json);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        patternStr = "\"" + chave + "\"\\s*:\\s*([^,\\}\\s]+)";
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(json);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return "desconhecido";
    }
}