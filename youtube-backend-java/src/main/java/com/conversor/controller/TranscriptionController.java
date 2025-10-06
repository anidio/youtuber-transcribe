package com.conversor.controller;

import com.conversor.service.GeminiService;
import com.conversor.service.OpenAIService;
import com.conversor.dto.TranscriptRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5174", "http://localhost:5173"})
public class TranscriptionController {

    private final GeminiService geminiService;
    private final OpenAIService openAIService;

    // CONSTRUTOR: Removido o YoutubeCaptionService obsoleto
    public TranscriptionController(GeminiService geminiService, OpenAIService openAIService) {
        this.geminiService = geminiService;
        this.openAIService = openAIService;
    }

    // NOVO ENDPOINT UNIFICADO para geração de conteúdo
    @PostMapping("/generate-description")
    public String generateDescription(@RequestBody TranscriptRequest request) {
        String inputContent = request.getTranscript();
        String platform = request.getPlatform();
        int limit = request.getCharacterLimit();

        if (inputContent == null || inputContent.trim().isEmpty()) {
            throw new IllegalArgumentException("O conteúdo de entrada (transcript) é obrigatório.");
        }
        if (platform == null || platform.isEmpty() || limit <= 0) {
            throw new IllegalArgumentException("A plataforma e o limite são obrigatórios e devem ser válidos.");
        }

        System.out.printf("✅ CONTEÚDO RECEBIDO. Tentando GEMINI (Plataforma: %s, Limite: %d)...\n", platform, limit);

        try {
            // 1. Tenta Gemini (Prioridade)
            return geminiService.generateContent(inputContent, platform, limit);

        } catch (WebClientResponseException e) {
            System.err.printf("⚠️ GEMINI FALHOU (%s). Tentando OpenAI como fallback...\n", e.getStatusCode());
            // 2. Tenta OpenAI (Fallback)
            return openAIService.generateContent(inputContent, platform, limit);

        } catch (Exception e) {
            throw new RuntimeException("Falha ao processar a descrição com ambas as IAs: " + e.getMessage());
        }
    }
}