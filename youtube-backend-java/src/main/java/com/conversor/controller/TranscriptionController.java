package com.conversor.controller;

import com.conversor.dto.TranscriptResponse;
import com.conversor.service.TranscriptionService;
// ⚠️ ATUALIZAÇÃO: Importa o GeminiService
import com.conversor.service.GeminiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5174"})
public class TranscriptionController {

    private final TranscriptionService transcriptionService;
    private final GeminiService geminiService; // ⚠️ NOVO SERVIÇO DE IA

    // Construtor atualizado para injetar GeminiService
    public TranscriptionController(TranscriptionService transcriptionService, GeminiService geminiService) {
        this.transcriptionService = transcriptionService;
        this.geminiService = geminiService;
    }

    // Endpoint 1: Transcrição Bruta
    @GetMapping("/transcribe")
    public TranscriptResponse transcribe(@RequestParam String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("A URL do YouTube é obrigatória.");
        }
        return transcriptionService.getTranscription(url);
    }

    // Endpoint 2: Resumir o Vídeo
    @GetMapping("/summarize")
    public String summarize(@RequestParam String url) {
        // 1. Obtém a transcrição (MOCK ATIVO)
        String transcript = transcriptionService.getTranscription(url).getTranscript();

        // 2. Chama a IA do Gemini para resumir
        return geminiService.summarize(transcript);
    }

    // Endpoint 3: Incrementar Conteúdo
    @GetMapping("/enrich")
    public String enrich(@RequestParam String url) {
        // 1. Obtém a transcrição (MOCK ATIVO)
        String transcript = transcriptionService.getTranscription(url).getTranscript();

        // 2. Chama a IA do Gemini para enriquecer
        return geminiService.enrich(transcript);
    }
}
