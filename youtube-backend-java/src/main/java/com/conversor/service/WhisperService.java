package com.conversor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class WhisperService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final WebClient webClient;

    public WhisperService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
    }

    // 🔹 1️⃣ Baixa o áudio do YouTube usando yt-dlp.exe
    private File downloadAudioFromYoutube(String videoUrl) throws IOException, InterruptedException {
        File outputFile = File.createTempFile("yt_audio_", ".mp3");

        // Comando yt-dlp para extrair o áudio
        ProcessBuilder pb = new ProcessBuilder(
                "yt-dlp",
                "-x",
                "--audio-format", "mp3",
                "-o", outputFile.getAbsolutePath(),
                videoUrl
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Aguarda até 2 minutos pelo download
        if (!process.waitFor(2, TimeUnit.MINUTES)) {
            process.destroy();
            throw new RuntimeException("Tempo limite ao baixar o áudio do YouTube.");
        }

        return outputFile;
    }

    // 🔹 2️⃣ Envia o áudio baixado para o Whisper API e retorna o texto
    public String transcribeAudio(String youtubeUrl) {
        File audioFile = null;
        try {
            // Baixa o áudio do vídeo
            audioFile = downloadAudioFromYoutube(youtubeUrl);

            // Faz a requisição pro Whisper
            String response = webClient.post()
                    .uri("/audio/transcriptions")
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .bodyValue(new FileSystemResource(audioFile))
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(error -> {
                        error.printStackTrace();
                        return Mono.just("Erro: " + error.getMessage());
                    })
                    .block();

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao transcrever áudio: " + e.getMessage();
        } finally {
            if (audioFile != null && audioFile.exists()) {
                audioFile.delete();
            }
        }
    }
}
