package com.conversor.controller;

import com.conversor.dto.TranscriptResponse;
import com.conversor.service.GeminiService;
import com.conversor.service.WhisperService; // Usaremos o serviço de áudio
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5174", "http://localhost:5173"})
public class TranscriptionController {

    // Mantemos os serviços de IA e adicionamos o serviço de transcrição de áudio
    private final WhisperService whisperService;
    private final GeminiService geminiService;

    public TranscriptionController(WhisperService whisperService, GeminiService geminiService) {
        this.whisperService = whisperService;
        this.geminiService = geminiService;
    }

    // Endpoint 1: Transcrição Bruta (Agora real via Whisper)
    @GetMapping("/transcribe")
    public TranscriptResponse transcribe(@RequestParam String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("A URL do YouTube é obrigatória.");
        }
        // 1. Obtém a transcrição via download/Whisper
        String transcript = whisperService.transcribeAudio(url);

        TranscriptResponse response = new TranscriptResponse();
        response.setVideoId("WHISPER_ID");
        response.setTranscript(transcript);
        return response;
    }

    // Endpoint 2: Resumir (Fluxo URL -> Whisper -> Gemini)
    @GetMapping("/summarize")
    public String summarize(@RequestParam String url) {
        // 1. Obtém a transcrição via download/Whisper
        String transcript = whisperService.transcribeAudio(url);
        if (transcript == null || transcript.trim().isEmpty()) {
            return "Falha na Transcrição. O áudio pode estar indisponível ou o Whisper falhou.";
        }
        // 2. Chama a IA do Gemini
        return geminiService.summarize(transcript);
    }

    // Endpoint 3: Incrementar Conteúdo (Fluxo URL -> Whisper -> Gemini)
    @GetMapping("/enrich")
    public String enrich(@RequestParam String url) {
        // 1. Obtém a transcrição via download/Whisper
        String transcript = whisperService.transcribeAudio(url);
        if (transcript == null || transcript.trim().isEmpty()) {
            return "Falha na Transcrição. O áudio pode estar indisponível ou o Whisper falhou.";
        }
        // 2. Chama a IA do Gemini
        return geminiService.enrich(transcript);
    }
}