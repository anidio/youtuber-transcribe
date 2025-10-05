package com.conversor.controller;

import com.conversor.dto.TranscriptResponse;
import com.conversor.service.TranscriptionService;
import com.conversor.service.GeminiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
// Mantenha o CORS para a porta do frontend
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5174", "http://localhost:5173"})
public class TranscriptionController {

    // Mantemos a injeção, mas só Gemini será usado diretamente
    private final TranscriptionService transcriptionService;
    private final GeminiService geminiService;

    public TranscriptionController(TranscriptionService transcriptionService, GeminiService geminiService) {
        this.transcriptionService = transcriptionService;
        this.geminiService = geminiService;
    }

    // ⚠️ O Endpoint /transcribe é mantido, mas não será usado pelo novo frontend.
    @GetMapping("/transcribe")
    public TranscriptResponse transcribe(@RequestParam String url) {
        // Devolve o mock para testar a conexão, mas o frontend será instruído a não usá-lo.
        return transcriptionService.getTranscription(url);
    }

    // 💡 FLUXO TACTIQ: Recebe o texto BRUTO no corpo da requisição (POST)
    @PostMapping("/summarize")
    public String summarize(@RequestBody String transcript) {
        if (transcript == null || transcript.trim().isEmpty()) {
            throw new IllegalArgumentException("O texto da transcrição é obrigatório. Cole a transcrição no campo de texto.");
        }
        // Chama a IA do Gemini diretamente
        return geminiService.summarize(transcript);
    }

    // 💡 FLUXO TACTIQ: Recebe o texto BRUTO no corpo da requisição (POST)
    @PostMapping("/enrich")
    public String enrich(@RequestBody String transcript) {
        if (transcript == null || transcript.trim().isEmpty()) {
            throw new IllegalArgumentException("O texto da transcrição é obrigatório. Cole a transcrição no campo de texto.");
        }
        // Chama a IA do Gemini diretamente
        return geminiService.enrich(transcript);
    }
}