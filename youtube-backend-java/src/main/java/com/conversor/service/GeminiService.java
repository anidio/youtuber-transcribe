package com.conversor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;

    // Endpoint oficial do Gemini
    private final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Método genérico para enviar prompts ao Gemini e receber a resposta textual.
     */
    private String getAiResponse(String prompt) {
        // Cria a estrutura da requisição JSON exigida pelo Gemini
        Map<String, Object> contentPart = Map.of("text", prompt);
        Map<String, Object> contents = Map.of(
                "contents", Collections.singletonList(
                        Map.of("parts", Collections.singletonList(contentPart))
                )
        );

        try {
            // Faz a requisição POST para a API do Gemini
            Map<String, Object> responseMap = webClient.post()
                    .uri(GEMINI_API_URL + apiKey)
                    .bodyValue(contents)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // Extrai o texto da resposta (navega na estrutura JSON)
            if (responseMap != null && responseMap.containsKey("candidates")) {
                var candidates = (java.util.List<Map<String, Object>>) responseMap.get("candidates");
                if (!candidates.isEmpty()) {
                    var content = (Map<String, Object>) candidates.get(0).get("content");
                    var parts = (java.util.List<Map<String, String>>) content.get("parts");
                    return parts.get(0).get("text");
                }
            }
            return "⚠️ Erro: resposta inesperada da API Gemini.";

        } catch (Exception e) {
            System.err.println("❌ Erro na comunicação com a API Gemini: " + e.getMessage());
            throw new RuntimeException("Falha ao se comunicar com a API Gemini.", e);
        }
    }

    // --- FUNCIONALIDADE 1: GERAÇÃO DE ROTEIRO/ESBOÇO (Endpoint: summarize) ---
    public String summarize(String inputContent) {
        String prompt = String.format(
                "Você é um Editor de Conteúdo Sênior. Gere um ROTEIRO COMPLETO (Esboço) para um artigo de blog otimizado para SEO baseado no texto de entrada. O roteiro deve ter de 5 a 7 tópicos, cada um sendo um subtítulo forte e atraente (H2) pronto para uso em um blog. Não use introduções como 'O esboço é'. Use apenas o formato Markdown de lista numerada. O texto base é: '%s'",
                inputContent
        );
        return getAiResponse(prompt);
    }

    // --- FUNCIONALIDADE 2: ARTIGO OTIMIZADO DE 500 PALAVRAS (Endpoint: enrich) ---
    public String enrich(String inputContent) {
        String prompt = String.format(
                "Você é um Copywriter e Especialista em SEO. Sua tarefa é transformar o texto de entrada em um artigo de blog completo, original e otimizado para rankear no Google, ideal para listagens de e-commerce ou reviews de afiliados. O artigo deve ter aproximadamente 500 palavras, usar subtítulos em Markdown (##) e um tom persuasivo. O texto base é: '%s'",
                inputContent
        );
        return getAiResponse(prompt);
    }
}