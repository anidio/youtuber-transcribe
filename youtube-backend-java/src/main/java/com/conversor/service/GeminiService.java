package com.conversor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.Map;

@Service
public class GeminiService {

    // Injeta a nova chave de API do application.properties
    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;
    // Endpoint para o Gemini Flash (modelo rápido e eficiente)
    private final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    private final String MODEL = "gemini-2.5-flash";

    public GeminiService(WebClient.Builder webClientBuilder) {
        // Inicializa o WebClient sem URL base (será definida com a chave no método)
        this.webClient = webClientBuilder.build();
    }

    /**
     * Função principal para comunicação com o Gemini.
     */
    private String getAiResponse(String prompt) {

        // Monta a estrutura de conteúdo exigida pelo Gemini
        Map<String, Object> contentPart = Map.of("text", prompt);
        Map<String, Object> contents = Map.of("contents", Collections.singletonList(Map.of("parts", Collections.singletonList(contentPart))));

        try {
            // Faz a requisição POST para o Gemini
            Map<String, Object> responseMap = webClient.post()
                    .uri(GEMINI_API_URL + apiKey) // Adiciona a chave na URL da requisição (padrão Gemini)
                    .bodyValue(contents) // Corpo da requisição
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // Extrai o texto da resposta (Navega na estrutura JSON do Gemini)
            if (responseMap != null && responseMap.containsKey("candidates")) {
                var candidates = (java.util.List<Map<String, Object>>) responseMap.get("candidates");
                if (!candidates.isEmpty()) {
                    var content = (Map<String, Object>) candidates.get(0).get("content");
                    var parts = (java.util.List<Map<String, String>>) content.get("parts");
                    return parts.get(0).get("text");
                }
            }
            return "Erro: Resposta inesperada da IA.";

        } catch (Exception e) {
            System.err.println("Erro na comunicação com a API Gemini: " + e.getMessage());
            throw new RuntimeException("Falha ao se comunicar com a API Gemini. Verifique sua chave e uso.", e);
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