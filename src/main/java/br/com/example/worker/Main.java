package br.com.example.worker;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        try {
            // ✅ Nome correto da variável de ambiente
            String baseUrl = System.getenv("API_BASE_URL") != null
                ? System.getenv("API_BASE_URL")
                : "http://host.docker.internal:8080";

            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

            // ✅ Header Accept adicionado
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/fila/primeiro"))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 404) {
                System.out.println("Fila vazia. Aguardando próximo ciclo.");
                return;
            }

            if (response.statusCode() != 200) {
                System.err.println("Erro inesperado da API: " + response.statusCode());
                return;
            }

            System.out.println("Item recebido: " + response.body());

            // ✅ Extraindo o id real da resposta
            Pattern pattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
            Matcher matcher = pattern.matcher(response.body());
            if (!matcher.find()) {
                System.err.println("id não encontrado na resposta");
                return;
            }
            int id = Integer.parseInt(matcher.group(1));

            // ✅ Lógica de processamento real aqui
            boolean sucesso = processarItem(response.body());

            // ✅ JSON de resultado baseado no sucesso real
            String status  = sucesso ? "CONCLUIDO" : "FALHOU";
            String resultado = sucesso ? "Processado com sucesso" : "Falha no processamento";
            String corpoJson = "{\"status\":\"" + status + "\",\"resultado\":\"" + resultado + "\"}";

            // ✅ ID dinâmico no PATCH
            HttpRequest patch = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/fila/" + id + "/resultado"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(corpoJson))
                .build();

            HttpResponse<String> patchResponse = client.send(
                patch,
                HttpResponse.BodyHandlers.ofString()
            );
            System.out.println("PATCH status: " + patchResponse.statusCode());

        } catch (InterruptedException e) {
            // ✅ InterruptedException tratado separadamente
            Thread.currentThread().interrupt();
            System.err.println("Execução interrompida: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro de comunicação: " + e.getMessage());
        }
    }

    // ✅ Método separado para a lógica de negócio
    private static boolean processarItem(String corpo) {
        // Implemente sua regra de negócio aqui
        System.out.println("Processando: " + corpo);
        return true;
    }
}