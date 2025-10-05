package com.conversor.controller;

import com.conversor.dto.TranscriptResponse;
import com.conversor.service.TranscriptionService;
import com.conversor.service.OpenAIService; // ⚠️ Novo import
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5174"})
public class TranscriptionController {

    private final TranscriptionService transcriptionService;
    private final OpenAIService openAIService; // Novo serviço de IA

    // Construtor atualizado para injetar ambos os serviços
    public TranscriptionController(TranscriptionService transcriptionService, OpenAIService openAIService) {
        this.transcriptionService = transcriptionService;
        this.openAIService = openAIService;
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
    // Ex: GET http://localhost:8080/api/videos/summarize?url=...
    @GetMapping("/summarize")
    public String summarize(@RequestParam String url) {
        // 1. Obtém a transcrição (funcionalidade 1)
        String transcript = transcriptionService.getTranscription(url).getTranscript();

        // 2. Chama a IA para resumir (funcionalidade 2)
        return openAIService.summarize(transcript);
    }

    // Endpoint 3: Incrementar Conteúdo
    // Ex: GET http://localhost:8080/api/videos/enrich?url=...
    @GetMapping("/enrich")
    public String enrich(@RequestParam String url) {
        // 1. Obtém a transcrição (funcionalidade 1)
        String transcript = transcriptionService.getTranscription(url).getTranscript();

        // 2. Chama a IA para enriquecer (funcionalidade 3)
        return openAIService.enrich(transcript);
    }
}