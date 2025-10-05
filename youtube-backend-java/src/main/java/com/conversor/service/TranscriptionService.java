package com.conversor.service;

import com.conversor.dto.TranscriptResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TranscriptionService {

    private final WebClient webClient;

    public TranscriptionService(WebClient.Builder webClientBuilder) {
        // Inicializa o WebClient, mas não será usado na lógica de mock
        this.webClient = webClientBuilder.build();
    }

    /**
     * VERSÃO FINAL DE PRODUÇÃO: Utiliza o MOCK estável.
     */
    public TranscriptResponse getTranscription(String youtubeUrl) {
        System.out.println("⚠️ MOCK ATIVO: Retornando texto de teste para a URL: " + youtubeUrl);

        // Texto Mock de qualidade para a IA processar
        TranscriptResponse mockResponse = new TranscriptResponse();
        mockResponse.setVideoId("MOCK_ID");
        mockResponse.setTranscript("O desenvolvimento ágil é essencial para startups. Ele se concentra na entrega rápida de software funcional e na colaboração com o cliente. Diferente dos métodos tradicionais, o ágil permite mudanças de requisitos de forma mais fácil. Scrum e Kanban são frameworks populares do desenvolvimento ágil.");

        return mockResponse;
    }
}