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
        Map<String, Object> contentPart = Map.of("text", prompt);
        Map<String, Object> contents = Map.of(
                "contents", Collections.singletonList(
                        Map.of("parts", Collections.singletonList(contentPart))
                )
        );

        try {
            Map<String, Object> responseMap = webClient.post()
                    .uri(GEMINI_API_URL + apiKey)
                    .bodyValue(contents)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

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
            return "Erro ao gerar resposta: " + e.getMessage();
        }
    }

    // ------------------------------------------------------------------------
    // FUNÇÕES PÚBLICAS USADAS PELO TranscriptionService
    // ------------------------------------------------------------------------

    /**
     * Gera um resumo estruturado do vídeo.
     */
    public String generateSummary(String transcript) {
        String prompt = """
            Você é um assistente especialista em resumos de vídeos do YouTube.
            Crie um resumo objetivo e bem formatado em até 5 parágrafos, explicando os principais pontos do vídeo abaixo:
            
            Texto:
            """ + transcript;
        return getAiResponse(prompt);
    }

    /**
     * Extrai os principais tópicos do conteúdo transcrito.
     */
    public String extractTopics(String transcript) {
        String prompt = """
            Extraia e liste os principais tópicos e ideias centrais do texto abaixo.
            Retorne em formato Markdown, com marcadores ("-") e no máximo 10 tópicos curtos e claros.

            Texto:
            """ + transcript;
        return getAiResponse(prompt);
    }

    /**
     * Melhora e reescreve a descrição do vídeo de forma mais atrativa.
     */
    public String enhanceDescription(String transcript) {
        String prompt = """
            Crie uma nova descrição otimizada e envolvente para o YouTube com base no texto a seguir.
            Use linguagem natural, amigável e que desperte curiosidade. Limite a 2 parágrafos curtos.

            Texto:
            """ + transcript;
        return getAiResponse(prompt);
    }

    // ------------------------------------------------------------------------
    // MÉTODOS ALTERNATIVOS (mantidos para compatibilidade, se já usados)
    // ------------------------------------------------------------------------

    /** Resumo mais direto em tópicos */
    public String summarize(String transcript) {
        String prompt = """
            Resuma o seguinte texto, que é uma transcrição de vídeo, em 5 a 7 tópicos curtos e claros para estudos.
            Use formatação Markdown com marcadores. NÃO inclua introduções como 'Este é um resumo do vídeo'.
            Texto:
            """ + transcript;
        return getAiResponse(prompt);
    }

    /** Enriquecimento do texto */
    public String enrich(String transcript) {
        String prompt = """
            O texto abaixo é uma transcrição de vídeo. Reescreva-o de forma coesa e profissional,
            corrigindo erros de fala e expandindo o conteúdo com informações complementares relevantes.
            Mantenha o tema, mas enriqueça o texto final para que pareça um artigo de blog.
            Limite o texto final a cerca de 500 palavras.

            Texto:
            """ + transcript;
        return getAiResponse(prompt);
    }
}
