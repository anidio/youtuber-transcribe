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
     * Função principal para comunicação com o GPT.
     */
    private String getAiResponse(String prompt) {

        // 1. Monta a Estrutura da Mensagem (formato exigido pela OpenAI)
        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );

        // 2. Monta o corpo da requisição POST
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

            // 4. Extrai o texto da resposta
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
}