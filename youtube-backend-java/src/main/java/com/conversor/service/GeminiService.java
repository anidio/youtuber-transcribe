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

    // Lógica para definir o estilo e a tarefa da IA com base na plataforma selecionada
    private String getStylePrompt(String platform) {
        switch (platform) {
            case "INSTAGRAM_CURTO":
                // NOVO PROMPT: Foco em título cativante, destaques e CTA de venda.
                return "Crie uma descrição de produto/serviço com um título cativante usando 1 a 2 emojis no início e fim. Liste 3 a 5 Destaques/Benefícios usando o emoji ✅ ou ✨. Inclua um Call to Action (CTA) forte com informações de preço/localização. Use um tom empolgante, muitas quebras de linha e emojis. Mantenha a saída com foco na venda.";
            case "TIKTOK_CASUAL":
                // NOVO PROMPT: Foco em linguagem da internet, fatos rápidos e hashtags de tendência.
                return "Crie uma legenda super casual e envolvente, usando linguagem da internet (ex: 'rolê', 'trend'). Liste 3 fatos rápidos sobre o produto e finalize com 3 a 5 hashtags que viralizam (ex: #unboxing #dicas). Use emojis de forma exagerada e respeite o limite de caracteres para legendas de vídeo.";
            case "ECOM_DIRETO":
                return "Crie uma descrição de produto/serviço com foco em clareza, listas de bullet points de benefícios e venda direta, sem emojis.";
            case "ROTEIRO_TOPICOS":
                return "Crie um ROTEIRO em tópicos numerados (Markdown #) de 5 a 7 itens para um artigo ou vídeo. Não inclua introduções.";
            case "SEO_LONGO":
            default:
                return "Crie um artigo de blog completo, original e otimizado para SEO, utilizando subtítulos em Markdown (##) e um tom persuasivo.";
        }
    }

    /**
     * MÉTODO UNIFICADO: Gera conteúdo dinâmico com base na plataforma e limite.
     */
    public String generateContent(String input, String platform, int limit) {
        String stylePrompt = getStylePrompt(platform);

        // Define se o limite é em caracteres ou palavras
        String limitType = platform.endsWith("_CURTO") || platform.endsWith("ECOM_DIRETO") ? "caracteres" : "palavras";

        String prompt = String.format(
                "Você é um Copywriter/Especialista em SEO. Baseado no conteúdo abaixo, %s O limite MÁXIMO de saída deve ser de %d %s. O conteúdo base é: '%s'",
                stylePrompt,
                limit,
                limitType,
                input
        );
        return getAiResponse(prompt);
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
}