package com.conversor.controller;

import com.conversor.dto.TranscriptResponse;
import com.conversor.service.GeminiService;
import com.conversor.service.WhisperService;
import com.conversor.service.YoutubeCaptionService; // Importar o novo serviço
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5174", "http://localhost:5173"})
public class TranscriptionController {

    private final WhisperService whisperService;
    private final GeminiService geminiService;
    private final YoutubeCaptionService youtubeCaptionService; // 💡 NOVO: Serviço de legendas

    // 💡 CONSTRUTOR ATUALIZADO para injetar o novo serviço
    public TranscriptionController(WhisperService whisperService, GeminiService geminiService, YoutubeCaptionService youtubeCaptionService) {
        this.whisperService = whisperService;
        this.geminiService = geminiService;
        this.youtubeCaptionService = youtubeCaptionService;
    }

    // 💡 NOVA LÓGICA: Tenta Legenda (rápido) -> Tenta Whisper (lento)
    private String getRobustTranscript(String url) {
        // 1. Tenta obter a transcrição via legenda (mais rápido e mais preciso)
        System.out.println("⏳ INICIANDO CAPTION: Tentando legenda para: " + url);
        String transcript = youtubeCaptionService.getCaptions(url);

        if (transcript == null || transcript.trim().isEmpty()) {
            // 2. Se falhar, tenta transcrição de áudio (Whisper)
            System.out.println("⚠️ LEGENDAS FALHARAM. INICIANDO WHISPER (Lento): " + url);
            transcript = whisperService.transcribeAudio(url);
        }
        return transcript;
    }

    // Endpoint 1: Transcrição Bruta
    @GetMapping("/transcribe")
    public TranscriptResponse transcribe(@RequestParam String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("A URL do YouTube é obrigatória.");
        }

        String transcript = getRobustTranscript(url); // Usa a lógica de fallback

        if (transcript == null || transcript.trim().isEmpty()) {
            throw new RuntimeException("Falha na Transcrição. O vídeo não tem legendas e o Whisper falhou.");
        }

        TranscriptResponse response = new TranscriptResponse();
        response.setVideoId("FALLBACK_ID"); // ID genérico, pois não extraímos de forma limpa aqui
        response.setTranscript(transcript);
        return response;
    }

    // Endpoint 2: Resumir (Fluxo URL -> Legenda/Whisper -> Gemini)
    @GetMapping("/summarize")
    public String summarize(@RequestParam String url) {
        String transcript = getRobustTranscript(url);

        if (transcript == null || transcript.trim().isEmpty()) {
            return "Falha na Transcrição. O áudio pode estar indisponível ou o Whisper falhou.";
        }
        // 2. Chama a IA do Gemini
        return geminiService.summarize(transcript);
    }

    // Endpoint 3: Incrementar Conteúdo (Fluxo URL -> Legenda/Whisper -> Gemini)
    @GetMapping("/enrich")
    public String enrich(@RequestParam String url) {
        String transcript = getRobustTranscript(url);

        if (transcript == null || transcript.trim().isEmpty()) {
            return "Falha na Transcrição. O áudio pode estar indisponível ou o Whisper falhou.";
        }
        // 2. Chama a IA do Gemini
        return geminiService.enrich(transcript);
    }
}