package com.conversor.service;

import com.conversor.dto.TranscriptResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TranscriptionService {

    @Autowired
    private GeminiService geminiService;

    // 💡 NOVO: Serviço de transcrição de áudio
    @Autowired
    private WhisperService whisperService;

    // Serviço para tentar a transcrição via legenda (fallback)
    @Autowired
    private YoutubeCaptionService youtubeCaptionService;

    // Lógica para extrair o ID do vídeo
    private String extractVideoId(String youtubeUrl) {
        Pattern pattern = Pattern.compile("(?:v=|youtu\\.be\\/|embed\\/|v\\/|shorts\\/)([0-9A-Za-z_-]{11})");
        Matcher matcher = pattern.matcher(youtubeUrl);
        return matcher.find() ? matcher.group(1) : "UNKNOWN_ID";
    }

    // Processa a URL inteira: Tenta Legenda -> Tenta Áudio -> Chama IA
    public TranscriptResponse processVideo(String youtubeUrl) {
        String videoId = extractVideoId(youtubeUrl);
        String finalTranscript;

        // 1. Tenta obter a transcrição via legenda (mais rápido e mais preciso)
        System.out.println("⏳ INICIANDO CAPTION: Tentando legenda para: " + videoId);
        finalTranscript = youtubeCaptionService.getCaptions(youtubeUrl);

        if (finalTranscript == null || finalTranscript.isEmpty()) {
            // 2. Se falhar, tenta transcrição de áudio (Whisper)
            System.out.println("⚠️ LEGENDAS FALHARAM. INICIANDO WHISPER (Lento): " + videoId);
            finalTranscript = whisperService.transcribeAudio(youtubeUrl);
        }

        if (finalTranscript == null || finalTranscript.isEmpty()) {
            throw new RuntimeException("Falha crítica na Transcrição. O vídeo não tem legendas e o Whisper falhou.");
        }

        // 3. Processa a IA
        System.out.println("✅ TRANSCRIÇÃO OBTIDA. Chamando a IA...");
        String summary = geminiService.summarize(finalTranscript);
        String improvedDescription = geminiService.enrich(finalTranscript);

        // O construtor é chamado com todos os 5 argumentos (DTO corrigido)
        return new TranscriptResponse(videoId, finalTranscript, summary, "N/A", improvedDescription);
    }
}