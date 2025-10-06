package com.conversor.controller;

import com.conversor.service.GeminiService;
import com.conversor.service.OpenAIService;
import com.conversor.dto.TranscriptRequest; // Necessário criar esta DTO
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5174", "http://localhost:5173"})
public class TranscriptionController {

    private final GeminiService geminiService;
    private final OpenAIService openAIService;

    // O YoutubeCaptionService não é mais necessário para este fluxo, mas será mantido vazio por enquanto.

    // CONSTRUTOR: Removido YoutubeCaptionService (temporariamente)
    public TranscriptionController(GeminiService geminiService, OpenAIService openAIService) {
        this.geminiService = geminiService;
        this.openAIService = openAIService;
    }

    // Endpoint 1: RESUMO / GERAÇÃO DE TÓPICOS (Reuso do endpoint 'summarize' com POST)
    @PostMapping("/summarize")
    public String summarize(@RequestBody TranscriptRequest request) {
        String inputContent = request.getTranscript();

        if (inputContent == null || inputContent.trim().isEmpty()) {
            throw new IllegalArgumentException("O conteúdo de entrada é obrigatório.");
        }

        System.out.println("✅ CONTEÚDO RECEBIDO. Tentando GEMINI (Resumo/Roteiro)...");

        try {
            // 1. Tenta Gemini (Prioridade)
            return geminiService.summarize(inputContent);

        } catch (WebClientResponseException e) {
            System.err.println("⚠️ GEMINI FALHOU (" + e.getStatusCode() + "). Tentando OpenAI (Resumo) como fallback...");
            // 2. Tenta OpenAI (Fallback)
            return openAIService.summarize(inputContent);

        } catch (Exception e) {
            throw new RuntimeException("Falha ao processar o resumo/roteiro com ambas as IAs: " + e.getMessage());
        }
    }

    // Endpoint 2: APRIMORAMENTO / GERAÇÃO DE ARTIGO SEO (Reuso do endpoint 'enrich' com POST)
    @PostMapping("/enrich")
    public String enrich(@RequestBody TranscriptRequest request) {
        String inputContent = request.getTranscript();

        if (inputContent == null || inputContent.trim().isEmpty()) {
            throw new IllegalArgumentException("O conteúdo de entrada é obrigatório.");
        }

        System.out.println("✅ CONTEÚDO RECEBIDO. Tentando GEMINI (Artigo Otimizado)...");

        try {
            // 1. Tenta Gemini (Prioridade)
            return geminiService.enrich(inputContent);

        } catch (WebClientResponseException e) {
            System.err.println("⚠️ GEMINI FALHOU (" + e.getStatusCode() + "). Tentando OpenAI (Artigo Otimizado) como fallback...");
            // 2. Tenta OpenAI (Fallback)
            return openAIService.enrich(inputContent);

        } catch (Exception e) {
            throw new RuntimeException("Falha ao processar o artigo com ambas as IAs: " + e.getMessage());
        }
    }

    // Removed /transcribe GET endpoint and related logic.
}