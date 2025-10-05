package com.conversor.service;

import com.conversor.dto.TranscriptResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TranscriptionService {

    // Endpoint do Microserviço Python
    private static final String PYTHON_API_URL = "http://localhost:5000/api/transcribe";
    private final WebClient webClient;

    public TranscriptionService(WebClient.Builder webClientBuilder) {
        // Inicializa o WebClient
        this.webClient = webClientBuilder.baseUrl(PYTHON_API_URL).build();
    }

    /**
     * FUNÇÃO REAL: Chama o microserviço Python para obter a transcrição.
     */
    public TranscriptResponse getTranscription(String youtubeUrl) {
        System.out.println("✅ MOCK DESATIVADO: Tentando obter transcrição real para a URL: " + youtubeUrl);

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.queryParam("url", youtubeUrl).build())
                    .retrieve()
                    .bodyToMono(TranscriptResponse.class)
                    .block();
        } catch (Exception e) {
            // Captura e relança erros de comunicação, como 404 do Python
            System.err.println("Erro ao obter transcrição do serviço Python: " + e.getMessage());
            // Mapeia o erro para a camada de controle/frontend
            throw new RuntimeException("Falha na transcrição do vídeo. O serviço Python pode estar offline ou o vídeo não tem legendas disponíveis.", e);
        }
    }
}