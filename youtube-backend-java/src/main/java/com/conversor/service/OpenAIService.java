package com.conversor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.Map;

@Service
public class OpenAIService {

    // Injeta a chave de API do application.properties
    @Value("${openai.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private final String MODEL = "gpt-3.5-turbo"; // Modelo rápido e custo-eficiente

    public OpenAIService(WebClient.Builder webClientBuilder) {
        // Inicializa o WebClient com a URL da API da OpenAI
        this.webClient = webClientBuilder.baseUrl(OPENAI_API_URL).build();
    }

    /**
     * Função principal para comunicação com o GPT.
     */
    private String getAiResponse(String prompt) {

        // 1. Monta a Estrutura da Mensagem (formato exigido pela OpenAI)
        // O GPT precisa de um array de 'messages', onde cada item é um objeto com 'role' e 'content'.
        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );

        // 2. Monta o corpo da requisição POST
        // 'temperature' controla a criatividade (0.7 é um bom meio termo)
        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", Collections.singletonList(message),
                "temperature", 0.7
        );

        try {
            // 3. Faz a requisição POST para a OpenAI
            Map<String, Object> responseMap = webClient.post()
                    .uri(OPENAI_API_URL)
                    .header("Authorization", "Bearer " + apiKey) // ⚠️ Autenticação com a chave
                    .bodyValue(requestBody) // Corpo da requisição
                    .retrieve()
                    .bodyToMono(Map.class) // Recebe a resposta como um Map genérico
                    .block();

            // 4. Extrai o texto da resposta (Navega na estrutura JSON)
            if (responseMap != null && responseMap.containsKey("choices")) {
                var choices = (java.util.List<Map<String, Object>>) responseMap.get("choices");
                if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                    var messageContent = (Map<String, String>) choices.get(0).get("message");
                    return messageContent.get("content");
                }
            }
            return "Erro: Resposta inesperada da IA.";

        } catch (Exception e) {
            System.err.println("Erro na comunicação com a OpenAI: " + e.getMessage());
            throw new RuntimeException("Falha ao se comunicar com a API da OpenAI.", e);
        }
    }

    // --- Funcionalidade 2: Resumo ---
    public String summarize(String transcript) {
        String prompt = "Resuma o seguinte texto, que é uma transcrição de vídeo, em 5 a 7 tópicos curtos e claros para estudos. Use formatação Markdown com marcadores. NÃO inclua introduções como 'Este é um resumo do vídeo'. Retorne apenas os tópicos. Texto: \n\n" + transcript;
        return getAiResponse(prompt);
    }

    // --- Funcionalidade 3: Incremento ---
    public String enrich(String transcript) {
        String prompt = "O texto abaixo é uma transcrição de vídeo. Reescreva-o de forma coesa e profissional, corrigindo erros de fala e expandindo o conteúdo com informações complementares relevantes. Mantenha o tema, mas enriqueça o texto final para que pareça um artigo de blog. Limite o texto final a cerca de 500 palavras. Texto: \n\n" + transcript;
        return getAiResponse(prompt);
    }
}